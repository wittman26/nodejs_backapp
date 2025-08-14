package com.acelera.fx.digitalsignature.application.usecase.impl;

import com.acelera.broker.entidades.basicas.component.VariableClient;
import com.acelera.broker.fx.db.domain.dto.DocumentSignature;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientBuildDfdRequestUseCase;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.locale.LocaleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;

import static com.acelera.fx.digitalsignature.infrastructure.util.TradeSignatureConstants.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateExpedientBuildDfdRequestUseCaseImpl implements CreateExpedientBuildDfdRequestUseCase {

    @Value("${create-expedient-request.source-app.url}")
    private String sourceAppUrlBasePath;

    @Value("${create-expedient-request.bucket}")
    private String s3Bucket;

    @Value("${create-expedient-request.folder}")
    private String s3folder;

    private final VariableClient variableClient;

    @Override
    public Mono<ExpedientRequest> buildDfdRequest(Tuple4<String, String, String, String> titleAndCenterData,
            List<ExpedientRequest.Clause> clauses, List<DocumentSignature> documentSignatures,
            CreateExpedientRequest request, String origin, List<ProductDocumentParameters> documentTypes,
            List<TradeSignerDto> signers, Long originId) {
        log.info("7. Creando Request para llamado a DFD");
        return getValidityDays()
            .map(validityDays -> {
                log.info("Validity Days: {}", validityDays);
                return buildDfdRequestWithDays(titleAndCenterData, clauses, documentSignatures, request, origin, documentTypes, signers, originId, validityDays);
            });

    }

    private ExpedientRequest buildDfdRequestWithDays(Tuple4<String, String, String, String> titleAndCenterData, List<ExpedientRequest.Clause> clauses, List<DocumentSignature> documentSignatures, CreateExpedientRequest request, String origin, List<ProductDocumentParameters> documentTypes, List<TradeSignerDto> signers, Long originId,
            Long validityDays) {

        String ownerName = titleAndCenterData.getT1();
        String ownerDocument = titleAndCenterData.getT2();
        String center = titleAndCenterData.getT3();
        String owner = titleAndCenterData.getT4();

        List<ExpedientRequest.Document> documents = buildDocumentRequestList(ownerName, ownerDocument, owner, documentSignatures, documentTypes, signers, request.getProductId(), origin, originId);

        var sourceAppUrl = sourceAppUrlBasePath + SOURCE_APP_URL;
        return ExpedientRequest.builder()
                .sourceApp(ExpedientRequest.SourceApp.builder()
                        .operCode(ORIGIN_EVENT.equals(origin) ? OPER_CODE_EVENT : OPER_CODE_TRADE + originId)
                        .code(ACELERA)
                        .url(sourceAppUrl)
                        .build())
                .startDate(LocalDateTime.now(ZoneOffset.UTC))
                .endDate(LocalDateTime.now(ZoneOffset.UTC).plusDays(validityDays))
                .centre(center)
                .typeReference(DERIVADO_DIV)
                .indicatorBusinnessMailBox(isJuridic(center))
                .indicatorParticularMailBox(!isJuridic(center))
                .clauses(clauses)
                .typeBox(B092)
                .catBox(DIVISAS)
                .productDesc(DERIVADO_DIV)
                .descExp(CONT_DER_DIV)
                .channel(CHAN_OFI)
                .docs(documents)
                .customerId(owner)
                .build();
    }

    private Mono<Long> getValidityDays() {
        return variableClient.find("FX_SIGNATURE_VALIDITY_DAYS")
                .switchIfEmpty(Mono.error(new RuntimeException("FX_SIGNATURE_VALIDITY_DAYS no encontrado: ")))
                .map(Long::parseLong)
                .onErrorResume(e -> {
                    log.error("VARIABLE calling error : {}", e.getMessage());
                    return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, e.getMessage()));
                });
    }

    private List<ExpedientRequest.Document> buildDocumentRequestList(
            String titularCode,String titularName,String titularDoc, List<DocumentSignature> documentSignatures, List<ProductDocumentParameters> documentTypes, List<TradeSignerDto> signers, String productId, String origin, Long originId
    ) {

        return documentTypes.stream()
                .map(docType -> {
                    DocumentSignature signature = documentSignatures.stream()
                            .filter(sig -> Objects.equals(sig.getIdTipDoc(), docType.getDocumentType()))
                            .findFirst()
                            .orElseThrow(() -> new RuntimeException("Document signature not found for type " + docType.getDocumentType()));

                    List<ExpedientRequest.Document.Signer> docSigners = buildSignerList(signers, titularCode, titularName);

                    boolean isPrecontractual = "Y".equalsIgnoreCase(docType.getIsPrecontractual());

                    return ExpedientRequest.Document.builder()
                            .typeDoc(docType.getDocumentalTypeDoc())
                            .documentCode(docType.getDocumentalCodeDoc())
                            .indPreContractual(isPrecontractual)
                            .personDocNumber(titularDoc)
                            .s3(ExpedientRequest.Document.S3.builder()
                                    .bucket(s3Bucket) // configurable por entorno
                                    .folder(s3folder)
                                    .key(signature.getNombreDocumento())
                                    .build())
                            .signers(docSigners)
                            .metadata(buildMetadata(
                                    signature.getNombreDocumento(),
                                    docType.getDocumentalCodeDoc(),
                                    isPrecontractual,
                                    productId,
                                    titularCode,
                                    titularDoc,
                                    origin,
                                    originId
                            ))
                            .build();
                })
                .toList();
    }

    private List<ExpedientRequest.Document.Signer> buildSignerList(
            List<TradeSignerDto> signers,
            String titularCode,
            String titularName
    ) {
        return IntStream.range(0, signers.size())
                .mapToObj(i -> {
                    TradeSignerDto signer = signers.get(i);

                    return ExpedientRequest.Document.Signer.builder()
                            .signingPerson(stripLeftZeros(signer.getSignerId()))
                            .identityDoc(signer.getDocument().getNumber())
                            .signingName(signer.getName())
                            .interventionType(signer.getInterventionType())
                            .locationSign(StringUtils.EMPTY)
                            .order(i + 1)
                            .represented(
                                    isJuridic(titularCode)
                                            ? List.of(ExpedientRequest.Document.Signer.Represented.builder()
                                            .representedCode(stripLeftZeros(titularCode))
                                            .representedName(titularName)
                                            .build())
                                            : List.of()
                            )
                            .build();
                })
                .toList();
    }

    private ExpedientRequest.Document.Metadata buildMetadata(
            String documentName,
            String documentCode,
            boolean isPrecontractual,
            String productId,
            String titularCode,
            String titularDoc,
            String origin,
            Long originId
    ) {
        String titulo = isPrecontractual ? TITULO_PRECONTRACTUAL : TITULO_CONTRACTUAL;
        String operacionId = (ORIGIN_EVENT.equals(origin) ? OPER_CODE_EVENT : OPER_CODE_TRADE) + originId;

        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        LocalDateTime maxDate = LocalDateTime.parse("9999-12-31T23:59:59.999");

        return ExpedientRequest.Document.Metadata.builder()
                .gnDate(now)
                .gnCreationDate(now)
                .gnDocOrig(true)
                .gnCaducityDate(maxDate)
                .gnName(documentName)
                .gnValidityDate(maxDate)
                .gnTitulo(titulo)
                .numPersonaCli(List.of(stripLeftZeros(titularCode)))
                .nomPlantilla(documentCode)
                .idEntidad(LocaleConstants.ENTITY_0049)
                .operacionId(operacionId)
                .claManuscrita(CLAUSULA_MANUSCRITA)
                .contPartenon(StringUtils.EMPTY)
                .producto(productId)
                .idOficialPers(List.of(titularDoc))
                .digitalizador(DIGITALIZADOR)
                .build();
    }

    private boolean isJuridic(String center) {
        return center.toUpperCase().startsWith("J");
    }

    private static String stripLeftZeros(String value) {
        return value == null ? null : value.charAt(0) + value.substring(1).replaceFirst("^0+(?!$)", StringUtils.EMPTY);
    }

}

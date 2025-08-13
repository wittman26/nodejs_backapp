package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.cso.db.shared.domain.dto.OperationDocument;
import com.acelera.broker.entidades.basicas.component.VariableClient;
import com.acelera.broker.fx.db.domain.dto.*;
import com.acelera.broker.fx.db.domain.port.*;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.broker.rest.dfd.domain.RestDfdClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureParameterDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.domain.port.service.CreateTradeSignatureExpedientService;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceGet;
import com.acelera.fx.digitalsignature.infrastructure.Constants;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;
import com.acelera.locale.LocaleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.IntStream;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTradeSignatureExpedientServiceImpl implements CreateTradeSignatureExpedientService {

    private final ProductDocumentsService productDocumentsService;
    private final DocumentSignatureRepositoryClient documentSignatureRepositoryClient;
    private final EventRepositoryClient eventRepositoryClient;
    private final OperationRepositoryClient operationRepositoryClient;
    private final HeadlineOperationRepositoryClient headlineOperationRepositoryClient;
    private final EventDisclaimerRepositoryClient eventDisclaimerRepositoryClient;
    private final OperationDisclaimerRepositoryClient operationDisclaimerRepositoryClient;
    private final TradeSignatureServiceGet tradeSignatureServiceGet;
    private final RestDfdClient restDfdClient;
    private final TradeSignerHelper tradeSignerHelper;
    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;
    private final VariableClient variableClient;

    @Value("${create-expedient-request.source-app.url}")
    private String sourceAppUrlBasePath;

    @Value("${create-expedient-request.bucket}")
    private String s3Bucket;

    @Value("${create-expedient-request.folder}")
    private String s3folder;

    @Override
    public Mono<CreateExpedientResponse> createSignatureExpedient(Locale locale, String entity, Long originId, CreateExpedientRequest request) {
        String origin = tradeSignerHelper.isEventProduct(request.getProductId()) ? "EVENT" : "TRADE";
        return startExpedientWorkFlow(locale, entity, originId, request, origin);
    }

    private Mono<CreateExpedientResponse> startExpedientWorkFlow(Locale locale, String entity, Long originId, CreateExpedientRequest request, String origin) {
        return obtainTradeSignature(entity, originId, request)
                .flatMap(tradeSignature -> {
                            if(tradeSignature.getExpedientId() != null) {
                                log.info("Expediente encontrado para tradeSignatureId: {}", tradeSignature.getTradeSignatureId());
                                return Mono.just(CreateExpedientResponse.builder()
                                                .expedientId(tradeSignature.getExpedientId())
                                        .build());
                            }
                            return obtainSigners(locale, entity, tradeSignature, originId, origin).flatMap(
                                    signers -> obtainDocumentTypes(entity, locale, request.getProductId()).flatMap(
                                            documentTypes -> buildResponse(documentTypes, locale, entity, originId, request,
                                                    origin, signers, tradeSignature)));
                        }
                );
    }


    private Mono<TradeSignature> obtainTradeSignature(String entity, Long originId, CreateExpedientRequest request) {
        return tradeSignatureServiceGet.getTradeSignature(entity, originId, request)
                .doOnNext(ts -> log.info("1: TradeSignatureId obtenido: {}", ts.getTradeSignatureId()))
                .switchIfEmpty(Mono.error(new RuntimeException("TradeSignature no encontrado")));
    }

    private Mono<List<TradeSignerDto>> obtainSigners(Locale locale, String entity, TradeSignature tradeSignature, Long originId, String origin) {
        var signerRequest = getSignerRequest(tradeSignature.getTradeSignatureId(), originId, origin);
        return tradeSignatureServiceGet.getTradeSignature(locale, entity, signerRequest)
                .map(GetTradeSignatureDto::getSigners)
                .doOnNext(signers -> {
                    log.info("2: Firmantes encontrados: {}", signers.size());
                    signers.forEach(signer -> log.info("SignerId: {}", signer.getSignerId()));
                });
    }

    private GetTradeSignatureParameterDto getSignerRequest(Long tradeSignatureId, Long originId, String origin) {
        return GetTradeSignatureParameterDto.builder()
                .tradeSignatureId(tradeSignatureId)
                .origin(origin)
                .originId(originId)
                .build();
    }

    private Mono<List<ProductDocumentParameters>> obtainDocumentTypes(String entity, Locale locale, String productId) {
        return productDocumentsService.findProductDocumentType(entity, locale, productId)
                .collectList()
                .doOnNext(documents -> log.info("3: Tipos de documentos por producto: {}", documents.size()));
    }

    private Mono<CreateExpedientResponse> buildResponse(List<ProductDocumentParameters> documentTypes, Locale locale, String entity, Long originId, CreateExpedientRequest request, String origin,
            List<TradeSignerDto> signers, TradeSignature tradeSignature) {
        log.info("3.1: CONSTRUIR RESPUESTA: {}", documentTypes.size());
        documentTypes.forEach(doc -> log.info("DocumentType: {} - {}", doc.getProduct(), doc.getDocumentType()));
        return obtainDocumentSignatures(documentTypes, entity, originId, origin)
                .flatMap(documentSignatures -> validateDocuments(documentSignatures, documentTypes)
                        .then(obtainTitleAndCenterData(entity, originId, origin))
                        .flatMap(titleAndCenterData ->
                                obtainClauses(entity, originId, request.getProductId())
                                .flatMap(clauses ->
                                        getValidityDays()
                                            .doOnNext(validityDays -> {
                                                var mesg = "Validity Days: " + validityDays;
                                                log.info(mesg);
                                                return buildAndCreateExpedient(titleAndCenterData, clauses, documentSignatures, request, origin, documentTypes, signers, originId, validityDays);
                                            })
                                        .flatMap(idExpedient -> updateTradeSignatureExpedient(idExpedient, tradeSignature))
                        )
                );
    }

    // obtenerNombreDocumentos
    private Mono<List<DocumentSignature>> obtainDocumentSignatures(List<ProductDocumentParameters> documentTypes, String entity, Long originId, String origin) {
        //TODO cambiar originId quemado -> final long originIdTemp = 106365L;
        List<Mono<DocumentSignature>> monos = documentTypes.stream()
                .map(docType -> {
                    DocumentRequest request = DocumentRequest.builder()
                            .entityId(entity)
                            .documentTypeId(docType.getDocumentType())
                            .operationId("TRADE".equals(origin) ? originId : null)
                            .eventId("EVENT".equals(origin) ? originId : null)
                            .build();
                    return "TRADE".equals(origin)
                            ? documentSignatureRepositoryClient.findByEntityAndOperationAndDocumentType(request)
                            : documentSignatureRepositoryClient.findByEntityAndEventAndDocumentType(request);
                })
                .toList();
        log.info("4. Nombre Documentos obtenidos: {}", monos.size());
        monos.forEach(doc -> doc.flatMap( d -> {
                    log.info("Documento: {} ", d.getNombreDocumento());
                    return Mono.empty();
                }
        ));
        return Flux.mergeSequential(monos).collectList();
    }

    private Mono<Void> validateDocuments(List<DocumentSignature> encontrados, List<ProductDocumentParameters> esperados) {
        if (encontrados.size() != esperados.size()) {
            return Mono.error(new RuntimeException("Algunos documentos no se encontraron."));
        }
        return Mono.empty();
    }

    private Mono<Tuple4<String, String, String, String>> obtainTitleAndCenterData(String entity, Long originId, String origin) {
        log.info("5. Obtener Datos titular y Centro");
        if ("EVENT".equals(origin)) {
            log.info("5.1 Buscando los datos en la BBDD, esquema ACELER");
            EventRequest eventRequest = EventRequest.builder().eventId(originId).entityId(entity).build();
            Mono<Event> eventMono = eventRepositoryClient.findByEntityAndEvent(eventRequest);
            return Mono.zip(
                    eventMono.map(Event::getOwnerName), // NOMBRE_TITULAR
                    eventMono.map(Event::getOwnerDocument), //DOCUMENTO_TITULAR
                    eventMono.map(Event::getCenterId), // IDCENT
                    eventMono.map(Event::getOwner)
            );
        } else {
            log.info("5.1 Buscando los datos en la BBDD, esquema ACELER - ACE_OPERACION y ACE_OPERACION_TITULARES");
            // TRADE: obtener desde ACE_OPERACION y ACE_OPERACION_TITULARES - Caso Si productId != AC
            Mono<Operation> operationMono = operationRepositoryClient.findByOperationIdAndEntityId(
                    OperationRequest.builder().operationId(originId).entityId(entity).build());
            Mono<HeadlineOperation> headlineMono = headlineOperationRepositoryClient.findByOperationIdAndEntityId(
                    HeadlineOperationRequest.builder().operationId(originId).entityId(entity).build());
            return Mono.zip(
                    headlineMono.map(HeadlineOperation::getName),
                    headlineMono.map(HeadlineOperation::getDocument),
                    operationMono.map(Operation::getCenterId),
                    operationMono.map(Operation::getOwner)
            );
        }
    }

    private Mono<List<ExpedientRequest.Clause>> obtainClauses(String entity, Long originId, String productId) {
        log.info("6. Obtener el listado de cláusulas de la operación");
        if (tradeSignerHelper.isEventProduct(productId)) {
            log.info("6.1 EVENT Obtener el listado en ACELER.ACE_EVENT_DISCLAIMER ");
            return eventDisclaimerRepositoryClient.findByEntityAndEventId(
                            EventDisclaimerRequest.builder().entity(entity).eventId(originId).build()
                    ).switchIfEmpty(Mono.error(new RuntimeException("Disclaimer no encontrado en ACELER.ACE_EVENT_DISCLAIMER: " + originId)))
                    .map(disclaimer -> List.of(
                            ExpedientRequest.Clause.builder()
                                    .idClause(disclaimer.getName())
                                    .clauseContent(disclaimer.getContent())
                                    .build()
                    ));
        } else {
            log.info("6.1 EVENT Obtener el listado en ACELER.ACE_OPERATION_DISCLAIMER ");
            return operationDisclaimerRepositoryClient.findByEntityAndTradeId(
                            // TODO delete tradeID - 1425L
                            OperationDisclaimerRequest.builder().entity(entity).tradeId(originId).build()
                    ).switchIfEmpty(Mono.error(new RuntimeException("Disclaimer no encontrado en ACELER.ACE_OPERATION_DISCLAIMER: " + originId)))
                    .map(disclaimer -> List.of(
                            ExpedientRequest.Clause.builder()
                                    .idClause(disclaimer.getName())
                                    .clauseContent(disclaimer.getContent())
                                    .build()
                    ));
        }
    }


    private Mono<CreateExpedientResponse> updateTradeSignatureExpedient(Long idExpedient, TradeSignature tradeSignature) {

        return Mono.just(CreateExpedientResponse.builder().expedientId(idExpedient).build());
//        tradeSignature.setExpedientId(idExpedient);
//        return tradeSignatureRepositoryClient.save(tradeSignature)
//                .map(id -> CreateExpedientResponse.builder().expedientId(idExpedient).build())
//                .onErrorResume(e -> {
//                    var errorMsg = "Error guardando tradesignature : " + e.getMessage();
//                    log.error(errorMsg);
//                    return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, errorMsg));
//                });
    }

    private Mono<Long> buildAndCreateExpedient(Tuple4<String, String, String, String> titleAndCenterData, List<ExpedientRequest.Clause> clauses, List<DocumentSignature> documentSignatures, CreateExpedientRequest request, String origin,
            List<ProductDocumentParameters> documentTypes, List<TradeSignerDto> signers, Long originId, Long days) {
        log.info("7. Generar el expediente de firma de la operación y guardar su número en la bbdd");
        String ownerName = titleAndCenterData.getT1();
        String ownerDocument = titleAndCenterData.getT2();
        String center = titleAndCenterData.getT3();
        String owner = titleAndCenterData.getT4();

        List<ExpedientRequest.Document> documents = buildDocumentRequestList(ownerName, ownerDocument, owner, documentSignatures, documentTypes, signers, request.getProductId(), origin, originId);

        //documents = Collections.emptyList();
        var sourceAppUrl = sourceAppUrlBasePath + "/v1/trades-signatures/expedients/{id}?status={status}";
        ExpedientRequest expedientRequest = ExpedientRequest.builder()
                .sourceApp(ExpedientRequest.SourceApp.builder()
                        .operCode("EVENT".equals(origin) ? "ACEV" : "ACE" + originId)
                        .code(Constants.ACELERA)
                        .url(sourceAppUrl)
                        .build())
                .startDate(LocalDateTime.now(ZoneOffset.UTC))
                // + X días sobre startDate (en UTC), parámetro que se saca de ACELER_ENTIDADES.SAFE_VARIABLE.VALOR cuando ACELER_ENTIDADES.SAFE_VARIABLE.NOMBRE = "FX_SIGNATURE_VALIDITY_DAYS"
                .endDate(LocalDateTime.now(ZoneOffset.UTC).plusDays(days))
                .centre(center)
                .typeReference(Constants.DERIVADO_DIV)
                .indicatorBusinnessMailBox(center.toUpperCase().startsWith("J")) //Si el cliente es una jurídica true
                .indicatorParticularMailBox(!center.toUpperCase().startsWith("J")) //Si el cliente es una jurídica false
                .clauses(clauses)
                .typeBox(Constants.B092)
                .catBox(Constants.DIVISAS)
                .productDesc(Constants.DERIVADO_DIV)
                .descExp(Constants.CONT_DER_DIV)
                .channel(Constants.CHAN_OFI)
                .docs(documents)
                .customerId(owner)
                .build();

        return Mono.just(954274L);

//        return restDfdClient.createExpedient(expedientRequest)
//                .switchIfEmpty(Mono.error(new RuntimeException("DFD no devolvió expedientId")))
//                .onErrorResume(e -> {
//                    var errorMsg = "DFD calling error : " + e.getMessage();
//                    log.error(errorMsg);
//                    return Mono.just(954274L);
//                    //return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, errorMsg));
//                });
    }

    private Mono<Long> getValidityDays() {
        return variableClient.findValueByNameAndEntity("FX_SIGNATURE_VALIDITY_DAYS", LocaleConstants.ENTITY_0049)
                .switchIfEmpty(Mono.error(new RuntimeException("FX_SIGNATURE_VALIDITY_DAYS no encontrado: ")))
                .map(days -> {
                    log.info("Dias encontrados: {} ",days);
                    return Long.parseLong(days);
                })
                .onErrorResume(e -> {
                            var errorMsg = "VARIABLE calling error : " + e.getMessage();
                            log.error(errorMsg);
                            return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, errorMsg));
                        }
                );
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
                            .locationSign("")
                            .order(i + 1)
                            .represented(
                                    titularCode.startsWith("J")
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
        String titulo = isPrecontractual ? "Información Precontractual FX" : "Información Contractual FX";
        String operacionId = ("EVENT".equals(origin) ? "ACEV" : "ACE") + originId;
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
                .idEntidad("0049")
                .operacionId(operacionId)
                .claManuscrita("Y")
                .contPartenon("")
                .producto(productId)
                .idOficialPers(List.of(titularDoc))
                .digitalizador("OSP")
                .build();
    }

    private static String stripLeftZeros(String value) {
        return value == null ? null : value.charAt(0) + value.substring(1).replaceFirst("^0+(?!$)", "");
    }
}

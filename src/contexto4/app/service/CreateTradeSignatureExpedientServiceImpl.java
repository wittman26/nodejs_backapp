package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.DocumentSignature;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.broker.rest.dfd.domain.RestDfdClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientBuildDfdRequestUseCase;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientGetClausesUseCase;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientGetDocumentNamesUseCase;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientGetTitleAndCenterUseCase;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureParameterDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.domain.port.service.CreateTradeSignatureExpedientService;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceGet;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateTradeSignatureExpedientServiceImpl implements CreateTradeSignatureExpedientService {

    private final ProductDocumentsService productDocumentsService;
    private final TradeSignatureServiceGet tradeSignatureServiceGet;
    private final RestDfdClient restDfdClient;
    private final TradeSignerHelper tradeSignerHelper;
    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    private final CreateExpedientBuildDfdRequestUseCase createExpedientBuildDfdRequestUseCase;
    private final CreateExpedientGetClausesUseCase createExpedientGetClausesUseCase;
    private final CreateExpedientGetTitleAndCenterUseCase createExpedientGetTitleAndCenterUseCase;
    private final CreateExpedientGetDocumentNamesUseCase createExpedientGetDocumentNamesUseCase;

    @Override
    public Mono<CreateExpedientResponse> createSignatureExpedient(Locale locale, String entity, Long originId, CreateExpedientRequest request) {
        String origin = tradeSignerHelper.isEventProduct(request.getProductId()) ? "EVENT" : "TRADE";
        return startExpedientWorkFlow(locale, entity, originId, request, origin);
    }

    private Mono<CreateExpedientResponse> startExpedientWorkFlow(Locale locale, String entity, Long originId, CreateExpedientRequest request, String origin) {
        return obtainTradeSignature(entity, originId, request)
                .flatMap(tradeSignature -> {
                    if(tradeSignature.getExpedientId() != null) {
                        log.info("Expediente {} encontrado para tradeSignatureId: {}", tradeSignature.getExpedientId(), tradeSignature.getTradeSignatureId());
                    }
                    return obtainSigners(locale, entity, tradeSignature, originId, origin).flatMap(
                            signers -> obtainDocumentTypes(entity, locale, request.getProductId()).flatMap(
                                    documentTypes -> buildResponse(documentTypes, locale, entity, originId, request,
                                            origin, signers, tradeSignature)));
                });
    }


    private Mono<TradeSignature> obtainTradeSignature(String entity, Long originId, CreateExpedientRequest request) {
        log.info("1: Obtener TradeSignatureId ");
        return tradeSignatureServiceGet.getTradeSignature(entity, originId, request)
                .doOnNext(ts -> log.info("TradeSignatureId obtenido: {}", ts.getTradeSignatureId()))
                .switchIfEmpty(Mono.error(new RuntimeException("TradeSignature no encontrado")));
    }

    private Mono<List<TradeSignerDto>> obtainSigners(Locale locale, String entity, TradeSignature tradeSignature, Long originId, String origin) {
        log.info("2: Obtener Firmantes: ");
        var signerRequest = getSignerRequest(tradeSignature.getTradeSignatureId(), originId, origin);
        return tradeSignatureServiceGet.getTradeSignature(locale, entity, signerRequest)
                .map(GetTradeSignatureDto::getSigners)
                .doOnNext(signers -> {
                    log.info("Firmantes encontrados: {}", signers.size());
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
        log.info("3: Obtener DocumentTypes");
        return productDocumentsService.findProductDocumentType(entity, locale, productId)
                .collectList()
                .doOnNext(documents -> log.info("Tipos de documentos por producto: {}", documents.size()));
    }

    private Mono<CreateExpedientResponse> buildResponse(List<ProductDocumentParameters> documentTypes, Locale locale, String entity, Long originId, CreateExpedientRequest request, String origin,
            List<TradeSignerDto> signers, TradeSignature tradeSignature) {
        log.info("3.1: CONSTRUIR RESPUESTA");
        documentTypes.forEach(doc -> log.info("DocumentType: {} - {}", doc.getProduct(), doc.getDocumentType()));

        return createExpedientGetDocumentNamesUseCase.obtainDocumentSignatures(documentTypes, entity, originId, origin)
                .flatMap(documentSignatures -> validateDocuments(documentSignatures, documentTypes)
                    .then(createExpedientGetTitleAndCenterUseCase.obtainTitleAndCenterData(entity, originId, origin))
                    .flatMap(titleAndCenterData ->
                            createExpedientGetClausesUseCase.obtainClauses(entity, originId, request.getProductId())
                        .flatMap(clauses ->
                            createExpedientBuildDfdRequestUseCase.buildDfdRequest(
                                    titleAndCenterData, clauses, documentSignatures, request, origin, documentTypes, signers, originId)
                                .flatMap(expedientRequest ->
                                    createExpedientFromDfd(expedientRequest)
                                        .flatMap(idExpedient -> updateTradeSignatureExpedient(idExpedient, tradeSignature))
                                )
                        )
                    )
                );
    }

    private Mono<Void> validateDocuments(List<DocumentSignature> encontrados, List<ProductDocumentParameters> esperados) {
        if (encontrados.size() != esperados.size()) {
            return Mono.error(new RuntimeException("Algunos documentos no se encontraron."));
        }
        return Mono.empty();
    }

    private Mono<CreateExpedientResponse> updateTradeSignatureExpedient(Long idExpedient, TradeSignature tradeSignature) {
        log.info("7.2 Persiste Número de expediente en TRADE_SIGNATURE");
        tradeSignature.setExpedientId(idExpedient);
        return tradeSignatureRepositoryClient.save(tradeSignature)
                .map(id -> CreateExpedientResponse.builder().expedientId(idExpedient).build())
                .onErrorResume(e -> {
                    var errorMsg = "Error guardando tradesignature : " + e.getMessage();
                    log.error(errorMsg);
                    return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, errorMsg));
                });
    }

    private Mono<Long> createExpedientFromDfd(ExpedientRequest expedientRequest) {
        log.info("7.1 Generar el expediente de firma de la operación");

        return restDfdClient.createExpedient(expedientRequest)
                .switchIfEmpty(Mono.error(new RuntimeException("DFD no devolvió expedientId")))
                .onErrorResume(e -> {
                    var errorMsg = "DFD calling error : " + e.getMessage();
                    log.error(errorMsg);
                    //TODO delete mono.just - return Mono.just(954274L);
                    return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, errorMsg));
                });
    }

}

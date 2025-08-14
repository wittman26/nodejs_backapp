package com.acelera.fx.digitalsignature.application.usecase.impl;

import com.acelera.broker.fx.db.domain.dto.DocumentSignature;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.broker.rest.dfd.domain.RestDfdClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.application.usecase.port.*;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateExpedientFinalStepImpl implements CreateExpedientFinalStep {

    private final CreateExpedientGetDocumentNamesUseCase getDocumentNames;
    private final CreateExpedientGetTitleAndCenterUseCase getTitleAndCenter;
    private final CreateExpedientGetClausesUseCase getClauses;
    private final CreateExpedientBuildDfdRequestUseCase buildDfdRequest;
    private final RestDfdClient restDfdClient;
    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    @Override
    public Mono<CreateExpedientResponse> createExpedient(List<ProductDocumentParameters> docTypes, Locale locale,
            String entity, Long originId, CreateExpedientRequest request, String origin, List<TradeSignerDto> signers,
            TradeSignature tradeSignature) {

        log.info("3.1 Iniciando construcción de expediente...");
        return getDocumentNames.obtainDocumentSignatures(docTypes, entity, originId, origin)
                .flatMap(documentSignatures -> validateDocuments(documentSignatures, docTypes)
                    .then(getTitleAndCenter.obtainTitleAndCenterData(entity, originId, origin, request.getProductId()))
                    .flatMap(titleAndCenter -> getClauses.obtainClauses(entity, originId, request.getProductId())
                        .flatMap(clauses -> buildDfdRequest
                            .buildDfdRequest(titleAndCenter, clauses, documentSignatures, request, origin, docTypes, signers, originId)
                            .flatMap(expedientRequest -> createExpedientFromDfd(expedientRequest)
                                    .flatMap(idExpedient -> updateTradeSignatureExpedient(idExpedient, tradeSignature)
                                        .thenReturn(CreateExpedientResponse.builder().expedientId(idExpedient).build())
                                    )
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

    private Mono<Long> createExpedientFromDfd(ExpedientRequest expedientRequest) {
        log.info("7.1 Generar el expediente de firma de la operación");

        return restDfdClient.createExpedient(expedientRequest)
                .switchIfEmpty(Mono.error(new RuntimeException("DFD no devolvió expedientId")))
                .onErrorResume(e -> {
                    var errorMsg = "DFD calling error : " + e.getMessage();
                    log.error(errorMsg);
                    return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, errorMsg));
                });
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
}

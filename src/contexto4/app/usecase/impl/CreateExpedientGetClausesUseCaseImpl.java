package com.acelera.fx.digitalsignature.application.usecase.impl;

import com.acelera.broker.fx.db.domain.dto.EventDisclaimerRequest;
import com.acelera.broker.fx.db.domain.dto.OperationDisclaimerRequest;
import com.acelera.broker.fx.db.domain.port.EventDisclaimerRepositoryClient;
import com.acelera.broker.fx.db.domain.port.OperationDisclaimerRepositoryClient;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientGetClausesUseCase;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateExpedientGetClausesUseCaseImpl implements CreateExpedientGetClausesUseCase {
    private final TradeSignerHelper tradeSignerHelper;
    private final EventDisclaimerRepositoryClient eventDisclaimerRepositoryClient;
    private final OperationDisclaimerRepositoryClient operationDisclaimerRepositoryClient;

    @Override
    public Mono<List<ExpedientRequest.Clause>> obtainClauses(String entity, Long originId, String productId) {
        log.info("6. Obtener el listado de cláusulas de la operación");
        if (tradeSignerHelper.isEventProduct(productId)) {
            log.info("6.1 EVENT Obtener el listado en ACELER.ACE_EVENT_DISCLAIMER ");
            return eventDisclaimerRepositoryClient.findByEntityAndEventId(
                            EventDisclaimerRequest.builder().entity(entity).eventId(originId).build()
                    ).switchIfEmpty(Mono.defer(() -> {
                        log.warn("Disclaimer no encontrado en ACELER.ACE_EVENT_DISCLAIMER: {}", originId);
                        return Mono.just(List.of());
                    }))
                    .map(disclaimer -> List.of(
                            ExpedientRequest.Clause.builder()
                                    .idClause(disclaimer.getName())
                                    .clauseContent(disclaimer.getContent())
                                    .build()
                    ));
        } else {
            log.info("6.1 TRADE Obtener el listado en ACELER.ACE_OPERATION_DISCLAIMER ");
            return operationDisclaimerRepositoryClient.findByEntityAndTradeId(
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
}

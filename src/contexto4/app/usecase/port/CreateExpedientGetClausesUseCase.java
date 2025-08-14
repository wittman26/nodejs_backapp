package com.acelera.fx.digitalsignature.application.usecase.port;

import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CreateExpedientGetClausesUseCase {
    Mono<List<ExpedientRequest.Clause>> obtainClauses(String entity, Long originId, String productId);
}

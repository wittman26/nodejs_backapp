package com.acelera.fx.digitalsignature.application.usecase.port;

import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

public interface CreateExpedientGetTitleAndCenterUseCase {
    Mono<Tuple4<String, String, String, String>> obtainTitleAndCenterData(String entity, Long originId, String origin);
}

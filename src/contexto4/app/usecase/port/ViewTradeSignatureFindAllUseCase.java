package com.acelera.fx.digitalsignature.application.usecase.port;

import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedient;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedientFindByFilterRequest;
import com.acelera.broker.shared.domain.PageDto;

import reactor.core.publisher.Mono;

public interface ViewTradeSignatureFindAllUseCase {

  Mono<PageDto<ViewTradeSignatureExpedient>> findAll(ViewTradeSignatureExpedientFindByFilterRequest request);

}

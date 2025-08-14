package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedient;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedientFindByFilterRequest;
import com.acelera.broker.shared.domain.PageDto;
import com.acelera.fx.digitalsignature.application.mapper.ViewTradeSignatureExpedientFilterMapper;
import com.acelera.fx.digitalsignature.application.usecase.port.ViewTradeSignatureFindAllUseCase;
import com.acelera.fx.digitalsignature.domain.port.service.ViewTradeSignatureExpedientStatusService;

import org.springframework.stereotype.Service;

import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class ViewTradeSignatureExpedientStatusServiceImpl implements ViewTradeSignatureExpedientStatusService {

  private final ViewTradeSignatureFindAllUseCase viewTradeSignatureFindAllUseCase;

  @Override
  public Mono<ViewTradeSignatureExpedient> getSignatureExpedientStatus(String entity, Long originId) {
    return toRequest(entity, originId).flatMap(viewTradeSignatureFindAllUseCase::findAll)
      .flatMapIterable(PageDto::getContent).next()
      .switchIfEmpty(Mono.error(() -> new IllegalArgumentException(
        "No signature expedient found for entity: %s and originId: %s".formatted(entity, originId))));
  }

  private static Mono<ViewTradeSignatureExpedientFindByFilterRequest> toRequest(String entity, Long originId) {
    return Mono.just(ViewTradeSignatureExpedientFilterMapper.INSTANCE.mapRequest(entity, originId));
  }

}

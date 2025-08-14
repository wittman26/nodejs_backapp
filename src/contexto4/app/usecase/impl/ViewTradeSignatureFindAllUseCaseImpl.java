package com.acelera.fx.digitalsignature.application.usecase.impl;

import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedient;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedientFindByFilterRequest;
import com.acelera.broker.fx.db.domain.port.ViewTradeSignatureRepositoryClient;
import com.acelera.broker.shared.domain.PageDto;
import com.acelera.fx.digitalsignature.application.usecase.port.ViewTradeSignatureFindAllUseCase;

import org.springframework.stereotype.Component;

import reactor.core.publisher.Mono;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class ViewTradeSignatureFindAllUseCaseImpl implements ViewTradeSignatureFindAllUseCase {

  private final ViewTradeSignatureRepositoryClient  repositoryClient;

  @Override
  public Mono<PageDto<ViewTradeSignatureExpedient>> findAll(ViewTradeSignatureExpedientFindByFilterRequest request) {
    return repositoryClient.findByFilter(request);
  }
}

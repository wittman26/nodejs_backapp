package com.acelera.fx.digitalsignature.infrastructure.adapter.rest.controller;

import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.request.StartSignatureRequest;
import com.acelera.broker.fx.domain.dto.request.TradeSignatureRequest;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.StartSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.TradeSignatureResponse;
import com.acelera.fx.digitalsignature.domain.port.service.*;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.mapper.*;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.SignatureExpedienteStatusResponse;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.ui.TradeSignatureControllerRestUI;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class TradeSignatureRestController implements TradeSignatureControllerRestUI {

  private final TradeSignatureServiceSave tradeSignatureServiceSave;
  private final TradeSignatureServiceGet tradeSignatureServiceGet;
  private final ViewTradeSignatureExpedientStatusService statusService;
  private final UpdateSignatureExpedientStatusService updateSignatureExpedientStatusService;
  private final CreateTradeSignatureExpedientService createTradeSignatureExpedientService;
  private final TradeSignatureServicePost tradeSignatureServicePost;

  @Override
  public Mono<CreateExpedientResponse> createSignatureExpedient(String entity, Locale locale, Long originId,
          CreateExpedientRequest request) {
    return createTradeSignatureExpedientService.createSignatureExpedient(locale, entity, originId, request);
  }

  @Override
  public Mono<TradeSignatureResponse> updateTradeSignature(String entity, Locale locale,
      TradeSignatureRequest request) {
    var dto = TradeSignatureDtoMapper.INSTANCE.toTradeSignatureDto(request);
    return tradeSignatureServiceSave.createOrUpdateSignature(locale, entity, dto)
        .map(TradeSignatureResponseMapper.INSTANCE::fromDomain);
  }

  @Override
  public Mono<GetTradeSignatureResponse> getTradeSignature(String entity, Locale locale,
      GetTradeSignatureRequestParameter request) {
    var dto = GetTradeSignatureParameterDtoMapper.INSTANCE.toGetTradeSignatureParameterDto(request);
    return tradeSignatureServiceGet.getTradeSignature(locale, entity, dto)
        .map(GetTradeSignatureDtoMapper.INSTANCE::toGetTradeSignatureResponse);
  }

  @Override
  public Mono<SignatureExpedienteStatusResponse> getSignatureExpedientStatus(String entity, Long originId) {
    return statusService.getSignatureExpedientStatus(entity, originId)
        .map(SignatureExpedienteStatusResponse::fromDomain);
  }

  @Override
  public Mono<Void> updateSignatureExpedientStatus(String entity, @NotNull Long expedientId, @NotNull String status) {
    return updateSignatureExpedientStatusService.updateStatus(entity, expedientId, status);
  }

  @Override
  public Mono<StartSignatureResponse> postStartSignatureWorkflow(String entity, Locale locale, Long originId,
          StartSignatureRequest request, ServerHttpRequest httpRequest) {
      var dto = StartSignatureRequestDtoMapper.INSTANCE.toStartSignatureDto(request);
    return tradeSignatureServicePost.startSignatureWorkflow(entity, locale, originId, dto, httpRequest)
              .map(StartSignatureResponseDtoMapper.INSTANCE::toStartSignatureResponse);

  }

}

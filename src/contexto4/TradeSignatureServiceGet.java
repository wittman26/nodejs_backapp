package com.acelera.fx.digitalsignature.domain.port.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureParameterDto;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateExpedientRequest;
import reactor.core.publisher.Mono;

import java.util.Locale;

public interface TradeSignatureServiceGet {
    Mono<GetTradeSignatureDto> getTradeSignature(
            Locale locale, String entity, GetTradeSignatureParameterDto dto);
    Mono<TradeSignature> getTradeSignature(String entity, Long originId, CreateExpedientRequest request);
}

package com.acelera.fx.digitalsignature.application.usecase.port;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

public interface CreateExpedientTradeSignatureStep {
    Mono<TradeSignature> obtainTradeSignature(String entity, Long originId, CreateExpedientRequest request);
    Mono<List<TradeSignerDto>> obtainSigners(Locale locale, String entity, TradeSignature tradeSignature, Long originId, String origin);
}

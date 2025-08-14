package com.acelera.fx.digitalsignature.application.usecase.impl;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientTradeSignatureStep;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureParameterDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceGet;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateExpedientTradeSignatureStepIml implements CreateExpedientTradeSignatureStep {

    private final TradeSignatureServiceGet tradeSignatureServiceGet;

    @Override
    public Mono<TradeSignature> obtainTradeSignature(String entity, Long originId, CreateExpedientRequest request) {
        log.info("1:  Buscando TradeSignature para originId={} y entity={}", originId, entity);
        return tradeSignatureServiceGet.getTradeSignature(entity, originId, request)
                .switchIfEmpty(Mono.error(new RuntimeException("TradeSignature no encontrado")));
    }

    @Override
    public Mono<List<TradeSignerDto>> obtainSigners(Locale locale, String entity, TradeSignature tradeSignature,
            Long originId, String origin) {
        if(tradeSignature.getExpedientId() != null) {
            log.info("Expediente {} encontrado para tradeSignatureId: {}", tradeSignature.getExpedientId(), tradeSignature.getTradeSignatureId());
        }
        log.info("2. Obteniendo firmantes para tradeSignatureId={}", tradeSignature.getTradeSignatureId());
        var signerRequest = getSignerRequest(tradeSignature.getTradeSignatureId(), originId, origin);
        return tradeSignatureServiceGet.getTradeSignature(locale, entity, signerRequest)
                .map(GetTradeSignatureDto::getSigners)
                .doOnNext(signers -> log.info("Firmantes encontrados: {}", signers.size()));
    }

    private GetTradeSignatureParameterDto getSignerRequest(Long tradeSignatureId, Long originId, String origin) {
        return GetTradeSignatureParameterDto.builder()
                .tradeSignatureId(tradeSignatureId)
                .origin(origin)
                .originId(originId)
                .build();
    }
}

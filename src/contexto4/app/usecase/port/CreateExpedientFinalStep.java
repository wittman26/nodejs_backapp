package com.acelera.fx.digitalsignature.application.usecase.port;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

public interface CreateExpedientFinalStep {
    Mono<CreateExpedientResponse> createExpedient(List<ProductDocumentParameters> docTypes,
            Locale locale,
            String entity,
            Long originId,
            CreateExpedientRequest request,
            String origin,
            List<TradeSignerDto> signers,
            TradeSignature tradeSignature);
}

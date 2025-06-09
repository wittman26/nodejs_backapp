package com.acelera.fx.digitalsignature.application.service;

import com.acelera.fx.digitalsignature.domain.TradeSignatureDomainService;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureService;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateDocumentRequest;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.CreateDocumentResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignatureResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureServiceImpl implements TradeSignatureService {

    private final TradeSignatureDomainService tradeSignatureDomainService;

    @Override
    public Mono<CreateDocumentResponse> createDocument(String originId, Locale locale, String entity, CreateDocumentRequest request) {
        return Mono.empty();
    }

    /**
     * PUT /v1/trades-signatures : Create/Update signature trade
     */
    @Override
    public Mono<TradeSignatureResponse> createOrUpdateSignature(Locale locale, String entity, TradeSignatureRequest request) {
        tradeSignatureDomainService.validateCreateOrUpdateParams(request);

        // Si es actualización, buscar y actualizar; si no, crear nueva
        return tradeSignatureDomainService
                .findTradeSignature(request, entity)
                .flatMap(tradeSignatureFound -> tradeSignatureDomainService.upsertTradeSignature(tradeSignatureFound, request, entity)) // Logica de actualizar
                .switchIfEmpty(Mono.defer(() -> tradeSignatureDomainService.upsertTradeSignature(null, request, entity))); // Logica de salvar
    }

}

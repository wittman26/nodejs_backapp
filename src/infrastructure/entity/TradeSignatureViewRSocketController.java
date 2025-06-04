package com.acelera.fx.db.infrastructure.adapter.rsocket.controller;

import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.FxTradeSignatureExpedientView;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.FxTradeSignerDocumentStatusView;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.repository.FxTradeSignatureExpedientViewRepository;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.repository.FxTradeSignerDocumentStatusViewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
@RequiredArgsConstructor
public class TradeSignatureViewRSocketController {

    private final FxTradeSignatureExpedientViewRepository expedientViewRepository;
    private final FxTradeSignerDocumentStatusViewRepository signerDocumentStatusViewRepository;

    @MessageMapping("trade-signature-expedient.by-entity")
    public Flux<FxTradeSignatureExpedientView> findExpedientsByEntity(String entity) {
        return Flux.fromIterable(expedientViewRepository.findByEntity(entity));
    }

    @MessageMapping("trade-signature-expedient.by-expedient-id")
    public Flux<FxTradeSignatureExpedientView> findExpedientsByExpedientId(Long expedientId) {
        return Flux.fromIterable(expedientViewRepository.findByExpedientId(expedientId));
    }

    @MessageMapping("trade-signer-document-status.by-trade-signature-id")
    public Flux<FxTradeSignerDocumentStatusView> findSignerStatusByTradeSignatureId(Long tradeSignatureId) {
        return Flux.fromIterable(signerDocumentStatusViewRepository.findByTradeSignatureId(tradeSignatureId));
    }

    @MessageMapping("trade-signer-document-status.by-signer-id")
    public Flux<FxTradeSignerDocumentStatusView> findSignerStatusBySignerId(String signerId) {
        return Flux.fromIterable(signerDocumentStatusViewRepository.findBySignerId(signerId));
    }
}
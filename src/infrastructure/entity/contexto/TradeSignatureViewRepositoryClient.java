package com.acelera.broker.fx.db.domain.port;

import com.acelera.broker.fx.db.domain.dto.TradeSignatureExpedientView;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureViewFindRequest;
import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.broker.shared.domain.ResilienceConstants;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import reactor.core.publisher.Mono;

import java.util.List;

@CircuitBreaker(name = ResilienceConstants.BROKER_FX_DB)
public interface TradeSignatureViewRepositoryClient {

    @MessageMapping("trade-signature-view-expedient.findTradeSignatureViewExpedient")
    Mono<TradeSignatureExpedientView> findTradeSignatureViewExpedient(@Payload TradeSignatureViewFindRequest request);

    @MessageMapping("trade-signer-view-document-status.findTradeSignerViewDocument")
    Mono<List<TradeSignerDocumentStatusView>> findTradeSignerViewDocument(@Payload TradeSignatureViewFindRequest request);
}

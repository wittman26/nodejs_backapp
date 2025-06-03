package com.acelera.fx.db.infrastructure.adapter.rsocket.controller;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.data.PersistWebFluxUtils;
import com.acelera.fx.db.domain.port.persistence.TradeSignatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Controller
@RequiredArgsConstructor
public class TradeSignatureRSocketController implements TradeSignatureRepositoryClient {

    private final TradeSignatureRepository repository;

    @Override
    public Mono<TradeSignature> save(@Payload TradeSignature tradeSignature) {
        return PersistWebFluxUtils.save(() -> repository.save(tradeSignature))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<TradeSignature> find(@Payload TradeSignatureFindRequest request) {
        return Mono.fromCallable(() -> repository.find(request)).flatMap(Mono::justOrEmpty)
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<TradeSignature> update(Integer tradeSignatureId, TradeSignature tradeSignature) {
        return null;
    }
}

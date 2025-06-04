package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud;

import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.TradeSignatureModel;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.Optional;

public interface SpringJpaTradeSignatureRepository extends CrudRepository<TradeSignatureModel, Long> {

    @EntityGraph(attributePaths = "tradeSignerList")
    @Query("SELECT t FROM TradeSignatureModel t WHERE "
            + "(:#{#filter.entity} IS NULL OR t.entity = :#{#filter.entity}) AND "
            + "(:#{#filter.originId} IS NULL OR t.originId = :#{#filter.originId}) AND "
            + "(:#{#filter.productId} IS NULL OR t.productId = :#{#filter.productId})")
    List<TradeSignatureModel> find(TradeSignatureFindRequest filter);

    @EntityGraph(attributePaths = "tradeSignerList")
    @NotNull Optional<TradeSignatureModel> findById(@NotNull Long id);
}


package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.repository;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.fx.db.domain.port.persistence.TradeSignatureRepository;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaTradeSignatureRepository;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.mapper.TradeSignatureMapper;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.TradeSignatureModel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaTradeSignatureRepositoryImpl implements TradeSignatureRepository {

    private final SpringJpaTradeSignatureRepository repository;

    @Override
    public TradeSignature save(TradeSignature tradeSignature) {
        TradeSignatureModel tradeSignatureModel = TradeSignatureMapper.INSTANCE.fromDomain(tradeSignature);

        if(tradeSignatureModel.getTradeSignerList()!= null) {
            tradeSignatureModel.getTradeSignerList()
                    .forEach(signer -> signer.setTradeSignatureModel(tradeSignatureModel));
        }

        var model = repository.save(tradeSignatureModel);
        return TradeSignatureMapper.INSTANCE.toDomain(model);
    }

    @Override
    public Optional<TradeSignature> find(TradeSignatureFindRequest request) {
        Optional<TradeSignatureModel> model;
        if(request.getTradeSignatureId() != null) {
             model = repository.findById(request.getTradeSignatureId());
        } else {
            var resul = repository.find(request);
            model = resul.stream().findFirst();
        }
        return model.map(TradeSignatureMapper.INSTANCE::toDomain);
    }
}


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

}
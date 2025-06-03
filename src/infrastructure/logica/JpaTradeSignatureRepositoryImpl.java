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

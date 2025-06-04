package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud;

import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.TradeSignerDocumentStatusModel;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface SpringJpaTradeSignerDocumentStatusViewRepository  extends
        CrudRepository<TradeSignerDocumentStatusView, Long> {
    List<TradeSignerDocumentStatusModel> findByTradeSignatureId(Long tradeSignatureId);
}


package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.repository;

import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.fx.db.domain.port.persistence.TradeSignerDocumentStatusViewRepository;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaTradeSignerDocumentStatusViewRepository;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.mapper.TradeSignerDocumentStatusViewMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JpaTradeSignerDocumentStatusViewRepositoryImpl implements TradeSignerDocumentStatusViewRepository {

    private final SpringJpaTradeSignerDocumentStatusViewRepository repository;

    @Override
    public Optional<TradeSignerDocumentStatusView> findTradeSignerDocumentStatusView(Long tradeSignatureId) {
        var resul = repository.findByTradeSignatureId(tradeSignatureId);
        var model = resul.stream().findFirst();
        return model.map(TradeSignerDocumentStatusViewMapper.INSTANCE::toDomain);
    }
}

Caused by: org.springframework.beans.factory.BeanCreationException: Error creating bean with name 'springJpaTradeSignerDocumentStatusViewRepository' defined in com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaTradeSignerDocumentStatusViewRepository defined in @EnableJpaRepositories declared on JpaRepositoriesRegistrar.EnableJpaRepositoriesConfiguration: Invocation of init method failed; nested exception is java.lang.IllegalArgumentException: Not a managed type: class com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView
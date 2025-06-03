package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.repository;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.data.jpa.PersistenceAutoConfig;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.mapper.TradeSignatureMapper;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;
import uk.co.jemos.podam.common.AttributeStrategy;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({ PersistenceAutoConfig.class, JpaTradeSignatureRepositoryImpl.class})
public class JpaTradeSignatureRepositoryImplTest {

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();
    private static final TradeSignatureMapper MAPPER = TradeSignatureMapper.INSTANCE;

    private @Autowired JpaTradeSignatureRepositoryImpl impl;
    private @Autowired EntityManager em;

    @BeforeEach
    void setUp() {
        AttributeStrategy<Long> idStrategy = (attrType, attrAnnotations) -> null;
        var strategy =
                PODAM_FACTORY.getStrategy().addOrReplaceAttributeStrategy(TradeSignatureModel.class, "id", idStrategy)
                        .addOrReplaceAttributeStrategy(TradeSignerModel.class, "id", idStrategy);
        PODAM_FACTORY.setStrategy(strategy);
    }

    @Test
    void testSave() {
        TradeSignatureModel model = PODAM_FACTORY.manufacturePojo(TradeSignatureModel.class);

        model.setTradeSignatureId(null);

        if(model.getTradeSignerList() != null) {
            for(TradeSignerModel signer: model.getTradeSignerList()) {
                signer.setTradeSignerId(null);
                signer.setTradeSignatureModel(model);
            }
        }

        TradeSignature tradeSignature = MAPPER.toDomain(model);
        var response = impl.save(tradeSignature);
        assertThat(response).as("save trade signature").usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*tradeSignatureId", ".*tradeSignerId", ".*usumodi", ".*fecmodi", ".*usualta", ".*fecalta")
                .isEqualTo(tradeSignature);
    }

    @Test
    void testSaveWithNoSigners() {
        TradeSignatureModel model = PODAM_FACTORY.manufacturePojo(TradeSignatureModel.class);
        model.setTradeSignatureId(null);
        model.setTradeSignerList(null); // Sin signers

        TradeSignature tradeSignature = MAPPER.toDomain(model);
        var response = impl.save(tradeSignature);

        assertThat(response.getTradeSignatureId()).isNotNull();
        assertThat(response.getTradeSignerList()).isNull();
    }
    
    @Test
    void testSaveAndFindById() {
        TradeSignatureModel model = PODAM_FACTORY.manufacturePojo(TradeSignatureModel.class);
        model.setTradeSignatureId(null);

        if(model.getTradeSignerList() != null) {
            for(TradeSignerModel signer: model.getTradeSignerList()) {
                signer.setTradeSignerId(null);
                signer.setTradeSignatureModel(model);
            }
        }

        TradeSignature tradeSignature = MAPPER.toDomain(model);
        TradeSignature saved = impl.save(tradeSignature);

        var foundOpt = impl.find(TradeSignatureFindRequest.builder()
                .tradeSignatureId(saved.getTradeSignatureId())
                .build());

        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get()).usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*usumodi", ".*fecmodi", ".*usualta", ".*fecalta")
                .isEqualTo(saved);
    }
    
    @Test
    void testFindByFilters() {
        TradeSignatureModel model = PODAM_FACTORY.manufacturePojo(TradeSignatureModel.class);
        model.setTradeSignatureId(null);

        if(model.getTradeSignerList() != null) {
            for(TradeSignerModel signer: model.getTradeSignerList()) {
                signer.setTradeSignerId(null);
                signer.setTradeSignatureModel(model);
            }
        }

        em.persist(model);
        em.flush();

        var foundOpt = impl.find(TradeSignatureFindRequest.builder()
                .entity(model.getEntity())
                .originId(model.getOriginId())
                .build());

        assertThat(foundOpt).isPresent();
        assertThat(foundOpt.get().getEntity()).isEqualTo(model.getEntity());
        assertThat(foundOpt.get().getOriginId()).isEqualTo(model.getOriginId());
    }
    
    @Test
    void testSaveWithSigners() {
        TradeSignatureModel model = PODAM_FACTORY.manufacturePojo(TradeSignatureModel.class);
        model.setTradeSignatureId(null);

        if(model.getTradeSignerList() != null) {
            for(TradeSignerModel signer: model.getTradeSignerList()) {
                signer.setTradeSignerId(null);
                signer.setTradeSignatureModel(model);
            }
        }

        TradeSignature tradeSignature = MAPPER.toDomain(model);
        var response = impl.save(tradeSignature);

        assertThat(response.getTradeSignerList()).isNotNull();
        assertThat(response.getTradeSignerList()).allMatch(s -> s.getTradeSignatureId() != null);
    }

    @Test
    void testSave() {
        TradeSignatureModel model = PODAM_FACTORY.manufacturePojo(TradeSignatureModel.class);

        model.setTradeSignatureId(null);

        if(model.getTradeSignerList() != null) {
            for(TradeSignerModel signer: model.getTradeSignerList()) {
                signer.setTradeSignerId(null);
                signer.setTradeSignatureModel(model);
            }
        }

        TradeSignature tradeSignature = MAPPER.toDomain(model);
        var response = impl.save(tradeSignature);

        assertThat(response).as("save trade signature").usingRecursiveComparison()
                .ignoringFieldsMatchingRegexes(".*tradeSignatureId", ".*tradeSignerId", ".*usumodi", ".*fecmodi", ".*usualta", ".*fecalta")
                .isEqualTo(tradeSignature);

        // Verifica que los IDs fueron asignados
        assertThat(response.getTradeSignatureId()).isNotNull();
        if(response.getTradeSignerList() != null) {
            assertThat(response.getTradeSignerList()).allMatch(s -> s.getTradeSignerId() != null);
        }
    }

}

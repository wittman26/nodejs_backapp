package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model;

import com.acelera.data.jpa.BaseAuditorJpa;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.List;

@Entity
@Table(name = "FX_TRADE_SIGNATURE")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class TradeSignatureModel extends BaseAuditorJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRADE_SIGNATURE_ID")
    private Long tradeSignatureId;

    @Size(max = 4)
    @Column(name = "ENTITY", nullable = false, length = 4)
    private String entity;

    @Column(name = "ORIGIN_ID", nullable = false)
    private Long originId;

    @Size(max = 20)
    @Column(name = "ORIGIN", nullable = false, length = 20)
    private String origin;

    @Size(max = 2)
    @Column(name = "PRODUCT_ID", nullable = false, length = 2)
    private String productId;

    @Size(max = 10)
    @Column(name = "SIGNATURE_TYPE", nullable = false, length = 10)
    private String signatureType;

    @Size(max = 1)
    @Column(name = "INDICATOR_SSCC", length = 1)
    private String indicatorSSCC;

    @Size(max = 10)
    @Column(name = "VALIDATED_BO", length = 10)
    private String validatedBo;

    @Column(name = "EXPEDIENT_ID")
    private Long expedientId;

    @OneToMany(mappedBy = "tradeSignatureModel", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TradeSignerModel> tradeSignerList;
}

package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model;

import com.acelera.data.jpa.BaseAuditorJpa;
import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Size;

@Entity
@Table(name = "FX_TRADE_SIGNER")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Setter
@Getter
public class TradeSignerModel extends BaseAuditorJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRADE_SIGNER_ID")
    private Long tradeSignerId;

    @Size(max = 1)
    @Column(name = "DOCUMENT_TYPE", nullable = false, length = 1)
    private String documentType;

    @Size(max = 20)
    @Column(name = "DOCUMENT_NUMBER", nullable = false, length = 20)
    private String documentNumber;

    @Size(max = 10)
    @Column(name = "SIGNER_ID", nullable = false, length = 10)
    private String signerId;

    @Size(max = 200)
    @Column(name = "NAME", nullable = false, length = 200)
    private String name;

    @Size(max = 1)
    @Column(name = "IS_CLIENT", nullable = false, length = 1)
    private String isClient;

    @Size(max = 2)
    @Column(name = "INTERVENTION_TYPE", nullable = false, length = 2)
    private String interventionType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRADE_SIGNATURE_ID", referencedColumnName = "TRADE_SIGNATURE_ID")
    private TradeSignatureModel tradeSignatureModel;
}

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
            model = Optional.ofNullable(resul.get(0));
        }
        return model.map(TradeSignatureMapper.INSTANCE::toDomain);
    }
}

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
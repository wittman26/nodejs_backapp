package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model;

import com.acelera.data.jpa.BaseAuditorJpa;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "FX_TRADE_SIGNATURE", schema = "ACELER_FX")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeSignatureModel extends BaseAuditorJpa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRADE_SIGNATURE_ID")
    private Long tradeSignatureId;

    @Column(name = "ENTITY", nullable = false, length = 4)
    private String entity;

    @Column(name = "ORIGIN_ID", nullable = false)
    private BigDecimal originId;

    @Column(name = "ORIGIN", nullable = false, length = 20)
    private String origin;

    @Column(name = "PRODUCT_ID", nullable = false, length = 2)
    private String productId;

    @Column(name = "SIGNATURE_TYPE", nullable = false, length = 10)
    private String signatureType;

    @Column(name = "INDICATOR_SSCC", length = 1)
    private String indicatorSscc;

    @Column(name = "VALIDATED_BO", length = 10)
    private String validatedBo;

    @Column(name = "EXPEDIENT_ID")
    private BigDecimal expedientId;
}

@Data
@EqualsAndHashCode(callSuper = true)
public class TradeSignature extends AuditZonedFields {

    private Integer tradeSignatureId;
    private String entity;
    private Long originId;
    private String origin;
    private String productId;
    private String signatureType;
    private String indicatorSSCC;
    private String validatedBo;
    private Long expedientId;
}

@Data
@NoArgsConstructor
@SuperBuilder
public abstract class AuditZonedFields {
    private String usualta;
    private ZonedDateTime fecalta;
    private String usumodi;
    private ZonedDateTime fecmodi;
}

package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.mapper;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.TradeSignatureModel;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TradeSignatureMapper {

    TradeSignatureMapper INSTANCE = Mappers.getMapper(TradeSignatureMapper.class);

    TradeSignatureModel fromDomain(TradeSignature tradeSignature);

    TradeSignature toDomain(TradeSignatureModel tradeSignatureModel);

}

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

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseAuditorJpa extends BaseAuditorSuperclass {

}
@Getter
@Setter
@NoArgsConstructor
@MappedSuperclass
@JsonIgnoreProperties(value = { "fecalta", "usualta", "usumodi", "fecmodi" })
public abstract class BaseAuditorSuperclass {

    @Column(nullable = false, updatable = false, length = 30)
    @Size(max = 30)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @CreatedBy
    private String usualta;

    @Column(nullable = false, updatable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @CreatedDate
    private LocalDateTime fecalta;

    @NotNull
    @Column(nullable = false, length = 30)
    @Size(max = 30)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @LastModifiedBy
    private String usumodi;

    @NotNull
    @Column(nullable = false)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    @LastModifiedDate
    private LocalDateTime fecmodi;
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
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface TradeSignatureMapper {
    TradeSignatureMapper INSTANCE = Mappers.getMapper(TradeSignatureMapper.class);

    @Mapping(target = "tradeSignatureId", source = "tradeSignatureId")
    @Mapping(target = "entity", source = "entity")
    @Mapping(target = "originId", expression = "java(tradeSignature.getOriginId() != null ? java.math.BigDecimal.valueOf(tradeSignature.getOriginId()) : null)")
    @Mapping(target = "origin", source = "origin")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "signatureType", source = "signatureType")
    @Mapping(target = "indicatorSscc", source = "indicatorSSCC")
    @Mapping(target = "validatedBo", source = "validatedBo")
    @Mapping(target = "expedientId", expression = "java(tradeSignature.getExpedientId() != null ? java.math.BigDecimal.valueOf(tradeSignature.getExpedientId()) : null)")
    // Auditoría
    @Mapping(target = "usuAlta", source = "usualta")
    @Mapping(target = "fecAlta", expression = "java(tradeSignature.getFecalta() != null ? java.time.LocalDateTime.from(tradeSignature.getFecalta()) : null)")
    @Mapping(target = "usuModi", source = "usumodi")
    @Mapping(target = "fecModi", expression = "java(tradeSignature.getFecmodi() != null ? java.time.LocalDateTime.from(tradeSignature.getFecmodi()) : null)")
    TradeSignatureModel fromDomain(TradeSignature tradeSignature);

    @Mapping(target = "tradeSignatureId", source = "tradeSignatureId")
    @Mapping(target = "entity", source = "entity")
    @Mapping(target = "originId", expression = "java(tradeSignatureModel.getOriginId() != null ? tradeSignatureModel.getOriginId().longValue() : null)")
    @Mapping(target = "origin", source = "origin")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "signatureType", source = "signatureType")
    @Mapping(target = "indicatorSSCC", source = "indicatorSscc")
    @Mapping(target = "validatedBo", source = "validatedBo")
    @Mapping(target = "expedientId", expression = "java(tradeSignatureModel.getExpedientId() != null ? tradeSignatureModel.getExpedientId().longValue() : null)")
    // Auditoría
    @Mapping(target = "usualta", source = "usuAlta")
    @Mapping(target = "fecalta", expression = "java(tradeSignatureModel.getFecAlta() != null ? tradeSignatureModel.getFecAlta().atZone(java.time.ZoneId.systemDefault()) : null)")
    @Mapping(target = "usumodi", source = "usuModi")
    @Mapping(target = "fecmodi", expression = "java(tradeSignatureModel.getFecModi() != null ? tradeSignatureModel.getFecModi().atZone(java.time.ZoneId.systemDefault()) : null)")
    TradeSignature toDomain(TradeSignatureModel tradeSignatureModel);
}

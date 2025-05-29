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
    private Long originId;

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
    private Long expedientId;

    @OneToMany(mappedBy = "tradeSignature", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private java.util.List<TradeSigner> tradeSigners;  
}

@Entity
@Table(name = "FX_TRADE_SIGNER", schema = "ACELER_FX")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TradeSigner {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TRADE_SIGNER_ID")
    private Long tradeSignerId;

    @Column(name = "TRADE_SIGNATURE_ID", nullable = false)
    private Long tradeSignatureId;

    @Column(name = "DOCUMENT_TYPE", nullable = false, length = 1)
    private String documentType;

    @Column(name = "DOCUMENT_NUMBER", nullable = false, length = 20)
    private String documentNumber;

    @Column(name = "SIGNER_ID", nullable = false, length = 10)
    private String signerId;

    @Column(name = "NAME", nullable = false, length = 200)
    private String name;

    @Column(name = "IS_CLIENT", nullable = false, length = 1)
    private String isClient;

    @Column(name = "INTERVENTION_TYPE", nullable = false, length = 2)
    private String interventionType;

    @Column(name = "FECALTA", nullable = false)
    private LocalDateTime fecAlta;

    @Column(name = "USUALTA", nullable = false, length = 30)
    private String usuAlta;

    @Column(name = "FECMODI", nullable = false)
    private LocalDateTime fecModi;

    @Column(name = "USUMODI", nullable = false, length = 30)
    private String usuModi;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "TRADE_SIGNATURE_ID", referencedColumnName = "TRADE_SIGNATURE_ID", insertable = false, updatable = false)
    private TradeSignatureModel tradeSignatureModel;
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
    private List<TradeSigner> tradeSigners;
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

@Data
@EqualsAndHashCode(callSuper = true)
public class TradeSigner extends AuditZonedFields {
    private Long tradeSignerId;
    private Long tradeSignatureId;
    private String documentType;
    private String documentNumber;
    private String signerId;
    private String name;
    private String isClient;
    private String interventionType;
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
    @Mapping(target = "originId", source = "originId")
    @Mapping(target = "origin", source = "origin")
    @Mapping(target = "productId", source = "productId")
    @Mapping(target = "signatureType", source = "signatureType")
    @Mapping(target = "indicatorSscc", source = "indicatorSSCC")
    @Mapping(target = "validatedBo", source = "validatedBo")
    @Mapping(target = "expedientId", source = "expedientId")
    @Mapping(target = "tradeSigners", source = "tradeSigners")
    // Auditoría
    @Mapping(target = "usualta", source = "usualta")
    @Mapping(target = "fecalta", expression = "java(tradeSignature.getFecalta() != null ? tradeSignature.getFecalta().toLocalDateTime() : null)")
    @Mapping(target = "usumodi", source = "usumodi")
    @Mapping(target = "fecmodi", expression = "java(tradeSignature.getFecmodi() != null ? tradeSignature.getFecmodi().toLocalDateTime() : null)")
    TradeSignatureModel fromDomain(TradeSignature tradeSignature);

    @Mapping(target = "tradeSigners", source = "tradeSigners")
    // Auditoría
    @Mapping(target = "usualta", source = "usualta")
    @Mapping(target = "fecalta", expression = "java(tradeSignatureModel.getFecalta() != null ? tradeSignatureModel.getFecalta().atZone(java.time.ZoneId.systemDefault()) : null)")
    @Mapping(target = "usumodi", source = "usumodi")
    @Mapping(target = "fecmodi", expression = "java(tradeSignatureModel.getFecmodi() != null ? tradeSignatureModel.getFecmodi().atZone(java.time.ZoneId.systemDefault()) : null)")
    TradeSignature toDomain(TradeSignatureModel tradeSignatureModel);
}


public class TradeSignatureMapperTest {
    private final TradeSignatureMapper MAPPER = TradeSignatureMapper.INSTANCE;

    private static final PodamFactoryImpl PODAM_FACTORY = new PodamFactoryImpl();

    @Test
    void testMapper() {

        TradeSignatureModel input = PODAM_FACTORY.manufacturePojo(TradeSignatureModel.class);

        TradeSignature expected = TradeSignature.builder()
                .tradeSignatureId(input.getTradeSignatureId().intValue())
                .entity(input.getEntity())
                .originId(input.getOriginId().longValue())
                .origin(input.getOrigin())
                .productId(input.getProductId())
                .signatureType(input.getSignatureType())
                .indicatorSSCC(input.getIndicatorSSCC())
                .validatedBo(input.getValidatedBo())
                .expedientId(input.getExpedientId().longValue())
                .build();

        TradeSignature result = MAPPER.toDomain(input);

        assertThat(result).as("TradeSignature").isNotNull().usingRecursiveComparison().isEqualTo(expected);
    }
}


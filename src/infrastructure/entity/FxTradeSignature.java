package com.acelera.fx.digitalsignature.infrastructure.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "FX_TRADE_SIGNATURE", schema = "ACELER_FX")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FxTradeSignature {
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

    @Column(name = "FECALTA", nullable = false)
    private LocalDateTime fecAlta;

    @Column(name = "USUALTA", nullable = false, length = 30)
    private String usuAlta;

    @Column(name = "FECMODI", nullable = false)
    private LocalDateTime fecModi;

    @Column(name = "USUMODI", nullable = false, length = 30)
    private String usuModi;

    @OneToMany(mappedBy = "tradeSignature", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    private java.util.List<FxTradeSigner> fxTradeSigners;
}

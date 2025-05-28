package com.acelera.fx.digitalsignature.infrastructure.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FX_TRADE_SIGNER", schema = "ACELER_FX")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FxTradeSigner {
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
    private FxTradeSignature tradeSignature;
}

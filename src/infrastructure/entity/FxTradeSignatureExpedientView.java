package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FX_VIEW_TRADE_SIGNATURE_EXPEDIENT")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FxTradeSignatureExpedientView {

    @Id
    @Column(name = "TRADE_SIGNATURE_ID")
    private Long tradeSignatureId;

    @Column(name = "ENTITY")
    private String entity;

    @Column(name = "ORIGIN_ID")
    private Long originId;

    @Column(name = "PRODUCT_ID")
    private String productId;

    @Column(name = "SIGNATURE_TYPE")
    private String signatureType;

    @Column(name = "VALIDATED_BO")
    private String validatedBo;

    @Column(name = "INDICATOR_SSCC")
    private String indicatorSSCC;

    @Column(name = "EXPEDIENT_ID")
    private Long expedientId;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @Column(name = "END_DATE")
    private LocalDateTime endDate;

    @Column(name = "EXPEDIENT_STATUS")
    private String expedientStatus;
}

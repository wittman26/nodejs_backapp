package com.acelera.broker.fx.db.domain.dto;

import lombok.*;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignatureExpedientView {
    private Long tradeSignatureId;
    private String entity;
    private Long originId;
    private String productId;
    private String signatureType;
    private String validatedBo;
    private String indicatorSSCC;
    private Long expedientId;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private String expedientStatus;
}

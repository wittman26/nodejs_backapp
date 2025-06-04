package com.acelera.broker.fx.db.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignerDocumentStatusView {

    private Long tradeSignatureId;
    private Long expedientId;
    private String signerId;
    private String documentType;
    private String documentNumber;
    private String name;
    private String isClient;
    private String interventionType;
    private String gnId;
    private String documentalTypeDoc;
    private String indicatorPrecontractual;
    private String signedDoc;
    private LocalDateTime signDate;
}

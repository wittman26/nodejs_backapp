package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignerDocumentStatusModelId implements Serializable {
    private Long tradeSignatureId;
    private String signerId;
    private String documentType;
    private String documentNumber;
}

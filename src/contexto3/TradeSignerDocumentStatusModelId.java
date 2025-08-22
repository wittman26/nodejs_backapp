package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignerDocumentStatusModelId implements Serializable {
    private static final long serialVersionUID = -1550188317157279868L;
    
    private Long tradeSignatureId;
    private String signerId;
    private String documentType;
    private String documentNumber;
    private String gnId;
}

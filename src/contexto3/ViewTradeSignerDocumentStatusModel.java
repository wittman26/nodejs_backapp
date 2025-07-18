package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model;

import org.hibernate.annotations.Immutable;
import lombok.*;


import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Immutable
@Table(name = "FX_VIEW_TRADE_SIGNER_DOCUMENT_STATUS")
@IdClass(TradeSignerDocumentStatusModelId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ViewTradeSignerDocumentStatusModel {

    @Id
    @Column(name = "TRADE_SIGNATURE_ID")
    private Long tradeSignatureId;

    @Column(name = "EXPEDIENT_ID")
    private Long expedientId;

    @Column(name = "SIGNER_ID")
    private String signerId;

    @Column(name = "DOCUMENT_TYPE")
    private String documentType;

    @Column(name = "DOCUMENT_NUMBER")
    private String documentNumber;

    @Column(name = "NAME")
    private String name;

    @Column(name = "IS_CLIENT")
    private String isClient;

    @Column(name = "INTERVENTION_TYPE")
    private String interventionType;

    @Column(name = "GN_ID")
    private String gnId;

    @Column(name = "DOCUMENTAL_TYPE_DOC")
    private String documentalTypeDoc;

    @Column(name = "INDICATOR_PRECONTRACTUAL", columnDefinition = "CHAR(1)")
    private String indicatorPrecontractual;

    @Column(name = "SIGNED_DOC", columnDefinition = "CHAR(1)")
    private String signedDoc;

    @Column(name = "SIGN_DATE")
    private LocalDateTime signDate;
}

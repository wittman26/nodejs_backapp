package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model;

import org.hibernate.annotations.Immutable;
import lombok.*;


import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
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

    @Id
    @Column(name = "SIGNER_ID")
    private String signerId;

    @Id
    @Column(name = "DOCUMENT_TYPE")
    private String documentType;

    @Id
    @Column(name = "DOCUMENT_NUMBER")
    private String documentNumber;

    @Column(name = "NAME")
    private String name;

    @Column(name = "IS_CLIENT")
    private String isClient;

    @Column(name = "INTERVENTION_TYPE")
    private String interventionType;

    @Id
    @Column(name = "GN_ID")
    private String gnId;

    @Column(name = "DOCUMENTAL_TYPE_DOC")
    private String documentalTypeDoc;

    @Size(max = 1)
    @Column(name = "INDICATOR_PRECONTRACTUAL", length = 1, columnDefinition = "CHAR(1)")
    private String indicatorPrecontractual;

    @Size(max = 1)
    @Column(name = "SIGNED_DOC", length = 1, columnDefinition = "CHAR(1)")
    private String signedDoc;

    @Column(name = "SIGN_DATE")
    private LocalDateTime signDate;
}

package infrastructure.entity;

package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FX_VIEW_TRADE_SIGNER_DOCUMENT_STATUS")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FxTradeSignerDocumentStatusView {

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
    private Long gnId;

    @Column(name = "DOCUMENTAL_TYPE_DOC")
    private String documentalTypeDoc;

    @Column(name = "INDICATOR_PRECONTRACTUAL")
    private String indicatorPrecontractual;

    @Column(name = "SIGNED_DOC")
    private String signedDoc;

    @Column(name = "SIGN_DATE")
    private LocalDateTime signDate;
}

Caused by: org.hibernate.tool.schema.spi.SchemaManagementException: Schema-validation: wrong column type encountered in column [indicator_precontractual] in table [fx_view_trade_signer_document_status]; found [char (Types#CHAR)], but expecting [varchar2(1 char) (Types#VARCHAR)]

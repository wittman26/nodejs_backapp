package com.acelera.dfd.db.infrastructure.adapter.persistence.jpa.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;

import com.acelera.broker.dfd.db.domain.dto.ExpedientStatus;
import com.acelera.data.BooleanYesNoConverter;
import com.acelera.data.jpa.BaseAuditorJpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DFD_EXPEDIENT", uniqueConstraints = @UniqueConstraint(columnNames = { "ID_EXP_SEC" }))
@Getter
@Setter
public class ExpedientModel extends BaseAuditorJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 4)
    @Column(length = 4, updatable = false, nullable = false)
    private String entity;

    @NotNull
    @Column(name = "ID_EXP_SEC", nullable = false)
    private Long idExpSec;

    @NotNull
    @Column(name = "ID_EXP_VERSION", nullable = false)
    private Integer idExpVersion;

    @Column(name = "SIGN_DOCS_PRECONTRACT", length = 1, nullable = false)
    @Convert(converter = BooleanYesNoConverter.class)
    private boolean signDocsPreContract;

    @Embedded
    private ExpedientSourceApp sourceApp;

    @Embedded
    private ExpedientSignChannel signChannel;

    @Column(name = "START_DATE")
    private LocalDateTime startDate;

    @NotNull
    @Column(name = "END_DATE", nullable = false)
    private LocalDateTime endDate;

    @NotNull
    @Column(name = "STATUS", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private ExpedientStatus status;

    @Column(name = "VALIDATED", length = 1)
    @Convert(converter = BooleanYesNoConverter.class)
    private Boolean validated;

    @NotBlank
    @Size(max = 4)
    @Column(name = "CENTRE", length = 4, nullable = false)
    private String centre;

    @NotBlank
    @Size(max = 30)
    @Column(name = "TYPE_REFERENCE", length = 30, nullable = false)
    private String typeReference;

    @Column(name = "INDICATOR_BUSINNESS_MAILBOX", length = 1)
    @Convert(converter = BooleanYesNoConverter.class)
    private Boolean indicatorBusinnessMailBox;

    @Column(name = "INDICATOR_PARTICULAR_MAILBOX", length = 1)
    @Convert(converter = BooleanYesNoConverter.class)
    private Boolean indicatorParticularMailBox;

    @Size(max = 20)
    @Column(name = "TYPE_BOX", length = 20)
    private String typeBox;

    @Size(max = 20)
    @Column(name = "CAT_BOX", length = 20)
    private String catBox;

    @Size(max = 30)
    @Column(name = "PRODUCT_DESC", length = 30)
    private String productDesc;

    @NotBlank
    @Size(max = 50)
    @Column(name = "DESC_EXP", length = 50, nullable = false)
    private String descExp;

    @NotBlank
    @Size(max = 3)
    @Column(name = "CHANNEL", length = 3, nullable = false)
    private String channel;

    @Size(max = 20)
    @Column(name = "CUSTOMER_ID", length = 20)
    private String customerId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinTable(name = "DFD_EXPEDIENT_DOCUMENT", joinColumns = { @JoinColumn(name = "EXPEDIENT_ID") },
            inverseJoinColumns = { @JoinColumn(name = "DOCUMENT_ID") })
    private Collection<DocumentModel> documents = new HashSet<>();

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "EXPEDIENT_ID", nullable = false)
    private Collection<ClauseModel> clauses = new HashSet<>();
}

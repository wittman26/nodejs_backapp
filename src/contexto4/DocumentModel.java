package com.acelera.dfd.db.infrastructure.adapter.persistence.jpa.model;

import java.util.Collection;
import java.util.HashSet;

import com.acelera.broker.dfd.db.domain.dto.DocumentStatus;
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
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DFD_DOCUMENT",
        uniqueConstraints = @UniqueConstraint(columnNames = { "BUCKET_S3", "FOLDER_S3", "KEY_S3" }))
@Getter
@Setter
public class DocumentModel extends BaseAuditorJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "STATUS", length = 50, nullable = false)
    @Enumerated(EnumType.STRING)
    private DocumentStatus status;

    @NotBlank
    @Size(max = 2)
    @Column(name = "DOCUMENT_CODE", length = 2, nullable = false)
    private String documentCode;

    @NotBlank
    @Size(max = 20)
    @Column(name = "PERSON_DOC_NUMBER", length = 20, nullable = false)
    private String personDocNumber;

    @Embedded
    private DocumentS3 s3;

    @NotBlank
    @Size(max = 40)
    @Column(name = "GN_ID", length = 40, nullable = false)
    private String gnId;

    @NotBlank
    @Size(max = 30)
    @Column(name = "TYPE_DOC", length = 30, nullable = false)
    private String typeDoc;

    @Column(name = "INDICATOR_PRECONTRACTUAL", length = 1, nullable = false)
    @Convert(converter = BooleanYesNoConverter.class)
    private boolean indPreContractual;

    @Lob
    @NotBlank
    @Column(columnDefinition = "CLOB", nullable = false)
    private String metadata;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "DOCUMENT_ID", nullable = false)
    private Collection<SignerModel> signers = new HashSet<>();
}
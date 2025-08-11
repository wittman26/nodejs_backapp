package com.acelera.dfd.db.infrastructure.adapter.persistence.jpa.model;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;

import com.acelera.data.BooleanYesNoConverter;
import com.acelera.data.jpa.BaseAuditorJpa;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DFD_SIGNER")
@Getter
@Setter
public class SignerModel extends BaseAuditorJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 10)
    @Column(name = "SIGNING_PERSON", length = 10, nullable = false)
    private String signingPerson;

    @Size(max = 200)
    @Column(name = "SIGNING_NAME", length = 200)
    private String signingName;

    @NotBlank
    @Size(max = 20)
    @Column(name = "IDENTITY_DOC", length = 20, nullable = false)
    private String identityDoc;

    @NotBlank
    @Size(max = 2)
    @Column(name = "INTERVENTION_TYPE", length = 2, nullable = false)
    private String interventionType;

    @Column(name = "SIGNED_DOC", length = 1)
    @Convert(converter = BooleanYesNoConverter.class)
    private Boolean signedDoc;

    @Column(name = "SIGN_DATE")
    private LocalDateTime signDate;
    
    @Size(max = 100)
    @Column(name = "LOCATION_SIGN", length = 100, nullable = false)
    private String locationSign;

    @NotNull
    @Column(name = "ORDER_SIGN", nullable = false)
    private Integer order;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "SIGNER_ID", nullable = false)
    private Collection<RepresentedModel> represented = new HashSet<>();
}
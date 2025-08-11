package com.acelera.dfd.db.infrastructure.adapter.persistence.jpa.model;

import com.acelera.data.jpa.BaseAuditorJpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DFD_EXPEDIENT_CLAUSES")
@Getter
@Setter
public class ClauseModel extends BaseAuditorJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 4)
    @Column(name = "ENTITY", length = 4, updatable = false, nullable = false)
    private String entity;

    @NotBlank
    @Column(name = "CLAUSE_ID", nullable = false)
    private String clauseId;

    @Lob
    @NotBlank
    @Column(name = "CLAUSE", nullable = false)
    private String clauseDescription;
}

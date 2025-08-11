package com.acelera.dfd.db.infrastructure.adapter.persistence.jpa.model;

import com.acelera.data.jpa.BaseAuditorJpa;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "DFD_REPRESENTED")
@Getter
@Setter
public class RepresentedModel extends BaseAuditorJpa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 10)
    @Column(name = "REPRESENTED_CODE", length = 10, nullable = false)
    private String representedCode;

    @NotBlank
    @Size(max = 200)
    @Column(name = "REPRESENTED_NAME", length = 200, nullable = false)
    private String representedName;
}

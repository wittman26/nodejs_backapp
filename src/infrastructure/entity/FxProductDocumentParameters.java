package com.acelera.fx.digitalsignature.infrastructure.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "FX_PRODUCT_DOCUMENT_PARAMETERS", schema = "ACELER_FX")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FxProductDocumentParameters {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "ENTITY", nullable = false, length = 4)
    private String entity;

    @Column(name = "PRODUCT", nullable = false, length = 2)
    private String product;

    @Column(name = "DOCUMENT_TYPE", nullable = false, length = 2)
    private String documentType;

    @Column(name = "IS_PRECONTRACTUAL", nullable = false, length = 1)
    private String isPrecontractual;

    @Column(name = "DOCUMENTAL_TYPE_DOC", nullable = false, length = 20)
    private String documentalTypeDoc;

    @Column(name = "DOCUMENTAL_CODE_DOC", nullable = false, length = 2)
    private String documentalCodeDoc;

    @Column(name = "FECALTA", nullable = false)
    private LocalDateTime fecAlta;

    @Column(name = "USUALTA", nullable = false, length = 30)
    private String usuAlta;

    @Column(name = "FECMODI", nullable = false)
    private LocalDateTime fecModi;

    @Column(name = "USUMODI", nullable = false, length = 30)
    private String usuModi;
}

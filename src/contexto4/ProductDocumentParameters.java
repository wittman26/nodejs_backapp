package com.acelera.broker.fx.db.domain.dto;

import lombok.Data;

@Data
public class ProductDocumentParameters {

    private Long id;
    private String entity;
    private String product;
    private String documentType;
    private String isPrecontractual;
    private String documentalTypeDoc;
    private String documentalCodeDoc;
}

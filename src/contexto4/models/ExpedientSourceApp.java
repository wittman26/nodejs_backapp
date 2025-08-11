package com.acelera.dfd.db.infrastructure.adapter.persistence.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class ExpedientSourceApp {
    @NotBlank
    @Size(max = 15)
    @Column(name = "SOURCE_APP_OPER_CODE", length = 15, nullable = false)
    private String operCode;

    @NotBlank
    @Size(max = 10)
    @Column(name = "SOURCE_APP_CODE", length = 10, nullable = false)
    private String code;

    @Size(max = 2048)
    @Column(name = "SOURCE_APP_URL", length = 2048)
    private String url;
}

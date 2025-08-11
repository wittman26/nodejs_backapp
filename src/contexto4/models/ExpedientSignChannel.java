package com.acelera.dfd.db.infrastructure.adapter.persistence.jpa.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Embeddable
@AllArgsConstructor
@NoArgsConstructor
public class ExpedientSignChannel {
    @Size(max = 15)
    @Column(name = "SIGNCHANNEL_CONTACT_POINT", length = 15)
    private String contactPoint;

    @Column(name = "SIGNCHANNEL_START_DATE")
    private LocalDateTime startDate;

    @Column(name = "SIGNCHANNEL_VALIDITY_DATE")
    private LocalDateTime validityDate;

    @Column(name = "SIGNCHANNEL_EXPIRATION_DAYS")
    private Integer expirationDays;
}

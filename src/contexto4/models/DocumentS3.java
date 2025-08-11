package com.acelera.dfd.db.infrastructure.adapter.persistence.jpa.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.validation.constraints.NotBlank;
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
public class DocumentS3 {
    @NotBlank
    @Size(max = 64)
    @Column(name = "BUCKET_S3", length = 64, nullable = false)
    private String bucket;

    @NotBlank
    @Size(max = 100)
    @Column(name = "FOLDER_S3", length = 100, nullable = false)
    private String folder;

    @NotBlank
    @Size(max = 100)
    @Column(name = "KEY_S3", length = 100, nullable = false)
    private String key;
}

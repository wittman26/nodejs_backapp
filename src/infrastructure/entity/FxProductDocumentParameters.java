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


public interface FxProductDocumentParametersRepository extends JpaRepository<FxProductDocumentParameters, Long> {
    // Buscar por el campo product
    java.util.List<FxProductDocumentParameters> findByProduct(String product);
    // Buscar por entity y product
    java.util.List<FxProductDocumentParameters> findByEntityAndProduct(String entity, String product);
}

public Flux<FxProductDocumentParameters> findByProduct(String product) {
    return Flux.fromIterable(fxProductDocumentParametersRepository.findByProduct(product));
}

public Flux<FxProductDocumentParameters> findByEntityAndProduct(String entity, String product) {
    return Flux.fromIterable(fxProductDocumentParametersRepository.findByEntityAndProduct(entity, product));
}

public Flux<DocumentTypeResponse> findDocumentTypesByProduct(String product) {
    Flux<FxProductDocumentParameters> parametersFlux = findByProduct(product);

    parametersFlux = parametersFlux.switchIfEmpty(
            Flux.error(new DigitalSignatureBusinessException("No document types found for product: " + product))
    );

    parametersFlux.map(param -> {
        if (param.getDocumentType() == null || param.getDocumentalTypeDoc() == null || param.getDocumentalCodeDoc() == null) {
            return Mono.error(new DigitalSignatureBusinessException("Invalid document type parameters for product: " + product));
        }
        log.info("Processing document type: {}", param.getDocumentType());
        return Mono.just(param);
    });

    return parametersFlux.map(param -> DocumentTypeResponse.builder()
            .documentType(param.getDocumentType())
            .isPrecontractual(param.getIsPrecontractual())
            .documentalTypeDoc(param.getDocumentalTypeDoc())
            .documentalCodeDoc(param.getDocumentalCodeDoc())
            .build());
}

public Flux<DocumentTypeResponse> findDocumentTypesByEntityAndProduct(String entity, String product) {
    Flux<FxProductDocumentParameters> parametersFlux = findByEntityAndProduct(entity, product);

    parametersFlux = parametersFlux.switchIfEmpty(
            Flux.error(new DigitalSignatureBusinessException("No document types found for entity: " + entity + ", product: " + product))
    );

    parametersFlux.map(param -> {
        if (param.getDocumentType() == null || param.getDocumentalTypeDoc() == null || param.getDocumentalCodeDoc() == null) {
            return Mono.error(new DigitalSignatureBusinessException("Invalid document type parameters for entity: " + entity + ", product: " + product));
        }
        log.info("Processing document type: {}", param.getDocumentType());
        return Mono.just(param);
    });

    return parametersFlux.map(param -> DocumentTypeResponse.builder()
            .documentType(param.getDocumentType())
            .isPrecontractual(param.getIsPrecontractual())
            .documentalTypeDoc(param.getDocumentalTypeDoc())
            .documentalCodeDoc(param.getDocumentalCodeDoc())
            .build());
}
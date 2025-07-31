package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParametersRequest;
import com.acelera.broker.fx.db.domain.port.ProductDocumentParametersRepositoryClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureRequestDto;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureResponseDto;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServicePost;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSignatureServicePostImpl implements TradeSignatureServicePost {

    private static final String CODIGO_ERROR_GENERAR_DOCUMENTOS = "error.fx.tradesignature.generaciondocumentos";
    private static final String CODIGO_ERROR_GENERAR_EXPEDIENTE = "error.fx.tradesignature.generacionexpediente";

    private final ProductDocumentParametersRepositoryClient productDocumentClient;

    @Override
    public Mono<StartSignatureResponseDto> startSignatureWorkflow(String entity, Locale locale, Long originId,
            StartSignatureRequestDto dto) {

        // 1. documentos del producto:
        return productDocumentClient.findProductDocumentParameters(
                        new ProductDocumentParametersRequest(entity, dto.getProductId()))
                .flatMap(doc -> generarDocumentos(doc, originId, dto.getProductId()))
                .collectList()
                .flatMap(documentos -> generarExpediente(originId, dto.getProductId()))
                .map(expedient ->
                        StartSignatureResponseDto.builder()
                                .expedientId(expedient.getExpedientId())
                                .build())
                .onErrorResume(e -> {
                    log.error("Error en generación de flujo de firma : {}", e.getMessage());
                    return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, e.getMessage()));
                });
    }

    private Mono<DocumentName> generarDocumentos(ProductDocumentParameters document, Long originId, String productId) {
        log.info("1. GENERACION DE DOCUMENTOS");
        log.info("Documento Base: {} - {} - {}", document.getDocumentType(), document.getDocumentalTypeDoc(), document.getIsPrecontractual());
        if (originId > 300) { // Simulación de error
            return Mono.error(new RuntimeException(CODIGO_ERROR_GENERAR_DOCUMENTOS));
        }
        log.info("Documento GENERADO - {}", document.getDocumentType() + originId + productId + ".pdf");
        return Mono.just(new DocumentName(document.getDocumentType() + originId + productId + ".pdf"));
    }

    private Mono<Expedient> generarExpediente(Long originId, String productId) {
        log.info("2. GENERACION DE EXPEDIENTE");
        log.info("Generar Expediente: {} - {}", originId, productId);
        if (originId < 0) { // Simulación de error
            return Mono.error(new RuntimeException(CODIGO_ERROR_GENERAR_EXPEDIENTE));
        }
        return Mono.just(new Expedient(998547 + originId));
    }

    @Getter
    static class DocumentName {
        private final String documentName;

        public DocumentName(String documentName) {
            this.documentName = documentName;
        }
    }

    @Getter
    static class Expedient {
        private final Long expedientId;

        public Expedient(Long expedientId) {
            this.expedientId = expedientId;
        }
    }
}

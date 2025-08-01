package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParametersRequest;
import com.acelera.broker.fx.db.domain.port.ProductDocumentParametersRepositoryClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureRequestDto;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureResponseDto;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServicePost;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.CreateExpedientResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSignatureServicePostImpl implements TradeSignatureServicePost {

    private static final String CODIGO_ERROR_GENERAR_DOCUMENTOS = "error.fx.tradesignature.generacion.documentos";
    private static final String CODIGO_ERROR_GENERAR_EXPEDIENTE = "error.fx.tradesignature.generacion.expediente";
    //private static final String CODIGO_ERROR_FIND_PRODUCT_DOCUMENT = "error.fx.product.document.id.notFound";
    private static final String CODIGO_ERROR_FIND_PRODUCT_DOCUMENT = "error.fx.tradesignature.id.notFound";

    private final ProductDocumentParametersRepositoryClient productDocumentClient;
    private final ProductDocumentsService productDocumentsService;
    private final TradeSignatureServiceSaveImpl tradeSignatureServiceSave;

    @Override
    public Mono<StartSignatureResponseDto> startSignatureWorkflow(String entity, Locale locale, Long originId,
            StartSignatureRequestDto dto) {
        String idProduct = dto.getProductId();

        // 1. documentos del producto:
        return productDocumentsService.findProductDocumentType(entity, locale,idProduct)
//        return productDocumentClient.findProductDocumentParameters(
//                        new ProductDocumentParametersRequest(entity, idProduct))
                .switchIfEmpty(Mono.error(CustomErrorException.ofArguments(NOT_FOUND, CODIGO_ERROR_FIND_PRODUCT_DOCUMENT, idProduct)))
                .flatMap(doc -> generarDocumentos(doc, originId, idProduct))
                .collectList()
                .flatMap(documentos -> generarExpediente(entity, locale, originId, idProduct))
                .map(expedient ->
                        StartSignatureResponseDto.builder()
                                .expedientId(expedient.getExpedientId())
                                .build())
                .onErrorResume(e -> {
                    if (e instanceof CustomErrorException) {
                        return Mono.error(e);
                    }
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

    private Mono<CreateExpedientResponse> generarExpediente(String entity, Locale locale, Long originId, String productId) {
        log.info("2. GENERACION DE EXPEDIENTE");
        log.info("Generar Expediente: {} - {}", originId, productId);
        var req = new CreateExpedientRequest(productId);
        return tradeSignatureServiceSave.createSignatureExpedient(locale, entity, originId, req);
//        if (originId < 0) { // Simulación de error
//            return Mono.error(new RuntimeException(CODIGO_ERROR_GENERAR_EXPEDIENTE));
//        }
//        return Mono.just(new Expedient(998547 + originId));
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

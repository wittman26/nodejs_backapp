package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.gateway.domain.dto.request.DocumentLpaCreateRequest;
import com.acelera.broker.gateway.domain.dto.response.DocumentLpaResponse;
import com.acelera.broker.gateway.domain.port.DocumentLpaClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.application.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureRequestDto;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureResponseDto;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServicePost;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;
import com.acelera.locale.LocaleConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import static com.acelera.fx.digitalsignature.application.util.TradeSignatureConstants.IS_PRECONTRACTUAL;
import static com.acelera.fx.digitalsignature.application.util.TradeSignatureConstants.PRODUCT_ID_FW;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSignatureServicePostImpl implements TradeSignatureServicePost {

    private static final String CODIGO_ERROR_GENERAR_DOCUMENTOS = "error.fx.tradesignature.generacion.documentos";
    private static final String CODIGO_ERROR_GENERAR_EXPEDIENTE = "error.fx.tradesignature.generacion.expediente";
    private static final String CODIGO_ERROR_POST_CONTRATACION = "error.fx.tradesignature.post-contratacion";
    private static final String CODIGO_ERROR_FIND_PRODUCT_DOCUMENT = "error.fx.product.document.id.notFound";

    private final ProductDocumentsService productDocumentsService;
    private final CreateTradeSignatureExpedientServiceImpl createTradeSignatureExpedientService;
    private final DocumentLpaClient documentLpaClient;
    private final TradeSignerHelper tradeSignerHelper;

    @Override
    public Mono<StartSignatureResponseDto> startSignatureWorkflow(String entity, Locale locale, Long originId,
            StartSignatureRequestDto dto, ServerHttpRequest httpRequest) {
        String productId = dto.getProductId();
        String token = extractToken(httpRequest);

        // 1. documentos del producto:
        return productDocumentsService.findProductDocumentType(entity, locale,productId)
                .switchIfEmpty(Mono.error(CustomErrorException.ofArguments(
                        NOT_FOUND, CODIGO_ERROR_FIND_PRODUCT_DOCUMENT, productId)))
                .collectList()
                .map(this::sortDocuments)
                .flatMap(sortedDocs -> generateAllDocuments(sortedDocs, originId, productId, token))
                .flatMap(__ -> generateExpedient(entity, locale, originId, productId))
                .map(this::mapToResponse)
                .onErrorResume(this::handleError);
    }

    private List<ProductDocumentParameters> sortDocuments(List<ProductDocumentParameters> documents) {
        return documents.stream()
                .sorted(Comparator.comparing(
                        doc -> !IS_PRECONTRACTUAL.equals(doc.getIsPrecontractual())))
                .toList();
    }

    private Mono<List<DocumentLpaResponse>> generateAllDocuments(List<ProductDocumentParameters> docs,
            Long originId,
            String productId,
            String token) {
        log.info("1. GENERACION DE DOCUMENTOS");
        return Flux.fromIterable(docs)
                .flatMap(doc -> {
                    log.info("Procesando documento {} (precontractual: {})", doc.getDocumentType(), doc.getIsPrecontractual());
                    return generateDocuments(doc, originId, productId, token);
                })
                .collectList()
                .doOnNext(list -> log.info("Documentos generados: {}", list.size()));
    }

    private Mono<DocumentLpaResponse> generateDocuments(ProductDocumentParameters document, Long originId, String productId,
            String token) {
        productId = tradeSignerHelper.isEventProduct(productId) ? PRODUCT_ID_FW : productId ;
        var request = createDocumentLpaCreateRequest(document, originId, token, productId);
        log.info("1.1 Generación Documento : type: {} - typeDoc: {} - isPrecontractual: {}", document.getDocumentType(), document.getDocumentalTypeDoc(), document.getIsPrecontractual());
        log.info("Iniciando llamado....REQUEST:  - {}", request);

        return documentLpaClient.generateDocumentLpa(request)
                .switchIfEmpty(Mono.error(new RuntimeException("Generate Document Lpa Service no devolvió documentId")))
                .map(documentLpaResponse -> {
                    log.info("Documento GENERADO - {}", documentLpaResponse.getNombreDocumento());
                    return documentLpaResponse;
                })
                .onErrorResume(e -> {
                    log.error("Error generando la documentación para el tipo {} : {}",request.getType() ,e.getMessage());
                    return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, CODIGO_ERROR_GENERAR_DOCUMENTOS,
                            request.getType(), e.getMessage()));
                });
    }

    private DocumentLpaCreateRequest createDocumentLpaCreateRequest(ProductDocumentParameters document, Long originId,
            String token, String productId) {
        return DocumentLpaCreateRequest.builder()
                .originId(originId)
                .type(document.getDocumentType())
                .productId(productId)
                .entity(LocaleConstants.ENTITY_0049)
                .locale(LocaleConstants.DEFAULT_LOCALE)
                .token(token)
                .build();
    }

    private Mono<CreateExpedientResponse> generateExpedient(String entity, Locale locale, Long originId, String productId) {
        var request = new CreateExpedientRequest(productId);
        log.info("2. GENERACION DE EXPEDIENTE");
        log.info("Iniciando llamado....Expediente REQUEST: {} - originId {}", request, originId);
        return createTradeSignatureExpedientService.createSignatureExpedient(locale, entity, originId, request)
                .onErrorResume(e -> {
                    log.error("Error en generación de expediente: {}", e.getMessage());
                    return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, CODIGO_ERROR_GENERAR_EXPEDIENTE,
                             e.getMessage()));
                });
    }

    private StartSignatureResponseDto mapToResponse(CreateExpedientResponse expedient) {
        log.info("Success - Expediente creado con id: {}", expedient.getExpedientId());
        return StartSignatureResponseDto.builder()
                .expedientId(expedient.getExpedientId())
                .build();
    }

    private <T> Mono<T> handleError(Throwable e) {
        if (e instanceof CustomErrorException) {
            return Mono.error(e);
        }
        log.error("Error en el proceso de gestión de datos de firma digital: {}", e.getMessage());
        return Mono.error(CustomErrorException.ofArguments(INTERNAL_SERVER_ERROR, CODIGO_ERROR_POST_CONTRATACION, e.getMessage()));
    }

    private String extractToken(ServerHttpRequest httpRequest) {
        return httpRequest.getHeaders().getFirst("Authorization");
    }

}

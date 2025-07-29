package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParametersRequest;
import com.acelera.broker.fx.db.domain.port.ProductDocumentParametersRepositoryClient;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureRequestDto;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureResponseDto;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServicePost;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSignatureServicePostImpl implements TradeSignatureServicePost {

    private final ProductDocumentParametersRepositoryClient productDocumentClient;

    @Override
    public Mono<StartSignatureResponseDto> startSignatureWorkflow(String entity, Locale locale, Long originId,
            StartSignatureRequestDto dto) {

        // 1. documentos del producto:
        return productDocumentClient.findProductDocumentParameters(
                new ProductDocumentParametersRequest(entity, dto.getProductId()))
                .doOnNext(doc -> generarDocumentos(doc, originId, dto.getProductId()))
                .then(
                    Mono.just(StartSignatureResponseDto.builder().expedientId(originId).build())
                );
    }

    private String generarDocumentos(ProductDocumentParameters document, Long originId, String productId) {
        if(document.getIsPrecontractual().equals("Y")) {
            log.info("Documento TRUE: {} - {} - {}", document.getDocumentType(), document.getDocumentalTypeDoc(), document.getIsPrecontractual());
        } else {
            log.info("Documento FALSE: {} - {} - {}", document.getDocumentType(), document.getDocumentalTypeDoc(), document.getIsPrecontractual());
        }

        return document.getDocumentType() + originId + productId + ".pdf";
    }

    private String generarExpediente(Long originId, String productId, String documentName) {
        log.info("Generar Expediente: {} - {} - Document Name: {}", originId, productId, documentName);
        return "998547";
    }
}

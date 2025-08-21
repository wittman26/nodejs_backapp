package com.acelera.fx.digitalsignature.application.usecase.impl;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientDocumentStep;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
@Slf4j
public class CreateExpedientDocumentStepImpl implements CreateExpedientDocumentStep {

    private final ProductDocumentsService productDocumentsService;

    @Override
    public Mono<List<ProductDocumentParameters>> obtainDocumentTypes(String entity, Locale locale, String productId) {
        log.info("3. Obteniendo tipos de documentos para producto={}", productId);
        return productDocumentsService.findProductDocumentType(entity, locale, productId)
                .collectList()
                .flatMap(docs -> {
                    if(docs.isEmpty()) {
                        return Mono.error(new RuntimeException("No se encontraron tipos de documento para el producto: " + productId));
                    }
                    log.info("Tipos de documento encontrados: {}", docs.size());
                    return Mono.just(docs);
                });

    }
}

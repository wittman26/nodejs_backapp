package com.acelera.fx.digitalsignature.infrastructure.controller;

import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.infrastructure.mapper.ProductDocumentsMapper;
import com.acelera.fx.digitalsignature.infrastructure.response.DocumentTypeResponse;
import com.acelera.fx.digitalsignature.infrastructure.ui.ProductDocumentsRestControllerUI;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class ProductDocumentsRestController implements ProductDocumentsRestControllerUI {

    private final ProductDocumentsService productDocumentsService;

    @Override
    public Flux<DocumentTypeResponse> findProductDocumentType(String entity, Locale locale, String productId) {
        return productDocumentsService.findProductDocumentType(entity, locale, productId)
                .map(ProductDocumentsMapper.INSTANCE::toDocumentTypeResponse);
    }
}

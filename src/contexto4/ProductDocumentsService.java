package com.acelera.fx.digitalsignature.domain.port.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import reactor.core.publisher.Flux;

import java.util.Locale;

public interface ProductDocumentsService {
    Flux<ProductDocumentParameters> findProductDocumentType(String entity, Locale locale, String productId);
}

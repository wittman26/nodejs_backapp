package com.acelera.fx.digitalsignature.application.usecase.port;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;

public interface CreateExpedientDocumentStep {
    Mono<List<ProductDocumentParameters>> obtainDocumentTypes(String entity, Locale locale, String productId);
}

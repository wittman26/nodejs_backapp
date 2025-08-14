package com.acelera.fx.digitalsignature.application.usecase.port;

import com.acelera.broker.fx.db.domain.dto.DocumentSignature;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import reactor.core.publisher.Mono;

import java.util.List;

public interface CreateExpedientGetDocumentNamesUseCase {
    Mono<List<DocumentSignature>> obtainDocumentSignatures(List<ProductDocumentParameters> documentTypes, String entity, Long originId, String origin);
}

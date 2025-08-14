package com.acelera.fx.digitalsignature.application.usecase.impl;

import com.acelera.broker.fx.db.domain.dto.DocumentRequest;
import com.acelera.broker.fx.db.domain.dto.DocumentSignature;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.port.DocumentSignatureRepositoryClient;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientGetDocumentNamesUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateExpedientGetDocumentNamesUseCaseImpl implements CreateExpedientGetDocumentNamesUseCase {
    private final DocumentSignatureRepositoryClient documentSignatureRepositoryClient;

    @Override
    public Mono<List<DocumentSignature>> obtainDocumentSignatures(List<ProductDocumentParameters> documentTypes,
            String entity, Long originId, String origin) {
        log.info("4. Obtener Nombre Documentos");
        //TODO cambiar originId quemado -> final long originIdTemp = 106365L;
        List<Mono<DocumentSignature>> monos = documentTypes.stream()
                .map(docType -> {
                    DocumentRequest request = DocumentRequest.builder()
                            .entityId(entity)
                            .documentTypeId(docType.getDocumentType())
                            .operationId("TRADE".equals(origin) ? originId : null)
                            .eventId("EVENT".equals(origin) ? originId : null)
                            .build();
                    return "TRADE".equals(origin)
                            ? documentSignatureRepositoryClient.findByEntityAndOperationAndDocumentType(request)
                            : documentSignatureRepositoryClient.findByEntityAndEventAndDocumentType(request);
                })
                .toList();
        log.info("Número de Documentos obtenidos: {}", monos.size());
        return Flux.mergeSequential(monos).collectList();
    }
}

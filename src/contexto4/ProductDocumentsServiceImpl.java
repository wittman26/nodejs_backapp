package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParametersRequest;
import com.acelera.broker.fx.db.domain.port.ProductDocumentParametersRepositoryClient;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductDocumentsServiceImpl implements ProductDocumentsService {

    private final ProductDocumentParametersRepositoryClient client;

    @Override
    public Flux<ProductDocumentParameters> findProductDocumentType(String entity, Locale locale, String productId) {
        Flux<ProductDocumentParameters> response = client.findProductDocumentParameters(new ProductDocumentParametersRequest(entity, productId));
        log.info("findProductDocumentType - {}", response);
        return response;
    }
}

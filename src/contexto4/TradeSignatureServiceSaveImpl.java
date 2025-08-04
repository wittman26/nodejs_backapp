package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.*;
import com.acelera.broker.fx.db.domain.port.*;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.broker.rest.dfd.domain.RestDfdClient;
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.application.mapper.TradeSignatureMapper;
import com.acelera.fx.digitalsignature.application.mapper.TradeSignatureRequestMapper;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureParameterDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceGet;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceSave;
import com.acelera.fx.digitalsignature.infrastructure.Constants;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.CreateExpedientResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureServiceSaveImpl implements TradeSignatureServiceSave {

    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;
    private final ProductDocumentsService productDocumentsService;
    private final DocumentSignatureRepositoryClient documentSignatureRepositoryClient;
    private final EventRepositoryClient eventRepositoryClient;
    private final OperationRepositoryClient operationRepositoryClient;
    private final HeadlineOperationRepositoryClient headlineOperationRepositoryClient;
    private final EventDisclaimerRepositoryClient eventDisclaimerRepositoryClient;
    private final OperationDisclaimerRepositoryClient operationDisclaimerRepositoryClient;
    private final TradeSignatureServiceGet tradeSignatureServiceGet;
    private final RestDfdClient restDfdClient;
    private final TradeSignerHelper tradeSignerHelper;

    @Override
    public Mono<TradeSignature> createOrUpdateSignature(Locale locale, String entity, TradeSignatureDto dto) {
        // Si es actualización, buscar y actualizar; si no, crear nueva
        return tradeSignatureRepositoryClient.find(TradeSignatureRequestMapper.INSTANCE.fromDtoToTradeSignatureFindRequest(dto, entity))
                .flatMap(tradeSignatureFound -> upsertTradeSignature(tradeSignatureFound, dto, entity)) // Logica de actualizar
                .switchIfEmpty(Mono.defer(() -> upsertTradeSignature(null, dto, entity))); // Logica de salvar
    }

    @Override
    public Mono<CreateExpedientResponse> createSignatureExpedient(Locale locale, String entity, Long originId, 
        CreateExpedientRequest request) {
        String origin = determineOrigin(request.getProductId());
        
        return getTradeSignatureWithSigners(entity, originId, request, origin)
                .flatMap(signers -> processDocuments(entity, locale, originId, request.getProductId(), origin, signers))
                .flatMap(context -> getOwnerAndCenterInfo(entity, originId, origin, context))
                .flatMap(context -> getDisclaimerContent(entity, originId, request.getProductId(), context))
                .flatMap(this::createDfdExpedient);
    }

    private String determineOrigin(String productId) {
        return tradeSignerHelper.isEventProduct(productId) ? "EVENT" : "TRADE";
    }

    private Mono<List<Signer>> getTradeSignatureWithSigners(String entity, Long originId, 
        CreateExpedientRequest request, String origin) {
        return tradeSignatureServiceGet.getTradeSignature(entity, originId, request)
                .flatMap(tradeSignature -> {
                    GetTradeSignatureParameterDto signerRequest = GetTradeSignatureParameterDto.builder()
                            .tradeSignatureId(tradeSignature.getTradeSignatureId())
                            .origin(origin)
                            .originId(originId)
                            .build();
                    return tradeSignatureServiceGet.getTradeSignature(locale, entity, signerRequest)
                            .map(GetTradeSignatureDto::getSigners);
                });
    }

    private Mono<ExpedientContext> processDocuments(String entity, Locale locale, Long originId, 
        String productId, String origin, List<Signer> signers) {
        return productDocumentsService.findProductDocumentType(entity, locale, productId)
                .collectList()
                .flatMap(documentTypes -> getDocumentSignatures(entity, originId, origin, documentTypes))
                .map(documents -> new ExpedientContext(signers, documents));
    }

    private Mono<List<DocumentSignature>> getDocumentSignatures(String entity, Long originId, 
        String origin, List<ProductDocumentType> documentTypes) {
        List<Mono<DocumentSignature>> documentSignaturesMonos = documentTypes.stream()
                .map(docType -> createDocumentRequest(entity, originId, docType, origin))
                .map(request -> getDocumentSignature(request, origin))
                .toList();

        return Flux.mergeSequential(documentSignaturesMonos)
                .collectList()
                .flatMap(signatures -> validateDocumentSignatures(signatures, documentTypes.size()));
    }

    private Mono<ExpedientContext> getOwnerAndCenterInfo(String entity, Long originId, 
        String origin, ExpedientContext context) {
        if ("EVENT".equals(origin)) {
            return getEventOwnerInfo(entity, originId, context);
        }
        return getTradeOwnerInfo(entity, originId, context);
    }

    private Mono<ExpedientContext> getDisclaimerContent(String entity, Long originId, 
        String productId, ExpedientContext context) {
        if (tradeSignerHelper.isEventProduct(productId)) {
            return getEventDisclaimerContent(entity, originId, context);
        }
        return getTradeDisclaimerContent(entity, originId, context);
    }

    private Mono<CreateExpedientResponse> createDfdExpedient(ExpedientContext context) {
        ExpedientRequest expedientRequest = buildExpedientRequest(context);
        return restDfdClient.createExpedient(expedientRequest)
                .flatMap(this::handleDfdResponse);
    }

    public Mono<TradeSignature> upsertTradeSignature(TradeSignature tradeSignatureFound, TradeSignatureDto dto, String entity) {
        // Lógica de actualización/creación
        TradeSignature tradeSignature = TradeSignatureMapper.INSTANCE.fromDtoToTradeSignature(dto, entity);

        // Si es actualización, conserva el ID, validatedBo, OriginID y limpia la lista anterior
        if (tradeSignatureFound != null) {
            if (tradeSignatureFound.getExpedientId() != null) {
                return Mono.error(CustomErrorException.ofArguments(BAD_REQUEST, "error.fx.tradesignature.expedient.exists"));
            }
            tradeSignature.setValidatedBo(tradeSignatureFound.getValidatedBo());
            tradeSignature.setOriginId(tradeSignatureFound.getOriginId());
            tradeSignature.setTradeSignatureId(tradeSignatureFound.getTradeSignatureId());
        } else {
            if (tradeSignature.getOriginId() == null) {
                return Mono.error(CustomErrorException.ofArguments(NOT_FOUND, "error.fx.tradesignature.id.notFound"));
            }
        }

        tradeSignature.setTradeSignerList(tradeSignature.getTradeSignerList());

        // Asegura que cada signer tenga el tradeSignatureId correcto
        tradeSignature.getTradeSignerList()
                .forEach(signer -> signer.setTradeSignatureId(tradeSignature.getTradeSignatureId()));

        return tradeSignatureRepositoryClient.save(tradeSignature);
    }

    // Clase auxiliar para mantener el contexto entre operaciones
    @Data
    @AllArgsConstructor
    private static class ExpedientContext {
        private List<Signer> signers;
        private List<DocumentSignature> documents;
        private String ownerName;
        private String ownerDocument;
        private String center;
        private String disclaimerContent;
        
        public ExpedientContext(List<Signer> signers, List<DocumentSignature> documents) {
            this.signers = signers;
            this.documents = documents;
        }
    }
}

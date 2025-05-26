package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSigner;
import com.acelera.broker.fx.db.domain.port.TradeSignerRepositoryClient;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureMapper;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignerMapper;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureService;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateDocumentRequest;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignerRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.CreateDocumentResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignatureResponse;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureServiceImpl implements TradeSignatureService {

    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    private final TradeSignerRepositoryClient tradeSignerRepositoryClient;

    private static final String ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE = "Se espera incluir originId o transferId pero no ambos";

    /**
     * Crea o actualiza una firma de operación.
     * @param locale el locale
     * @param entity la entidad
     * @param request la petición de firma
     * @return Mono con la respuesta de la firma
     */
    @Override
    public Mono<TradeSignatureResponse> createOrUpdateSignature(Locale locale, String entity, TradeSignatureRequest request) {
        boolean hasTradeSignatureId = request.getTradeSignatureId() != null;
        boolean hasOriginId = request.getOriginId() != null;

        validateCreateOrUpdateParams(hasTradeSignatureId, hasOriginId);

        if (hasTradeSignatureId) {
            // Lógica de Actualización
            return updateTradeSignature(request.getTradeSignatureId(), request);
        } else {
            // Consultar FX_TRADE_SIGNATURE, campo TRADE_SIGNATURE_ID
            return findTradeSignatureById(entity, request.getOriginId(), request.getProductId())
                    .flatMap(tradeSignatureResponse -> updateTradeSignature(tradeSignatureResponse.getTradeSignatureId(), request)) // Lógica de Actualización
                    .switchIfEmpty(saveTradeSignature(request)); // Lógica de Alta
        }
    }

    /**
     * Valida los parámetros de entrada para crear o actualizar una firma.
     */
    private void validateCreateOrUpdateParams(boolean hasTradeSignatureId, boolean hasOriginId) {
        if (hasTradeSignatureId == hasOriginId) {
            log.error(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
            throw new DigitalSignatureBusinessException(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
        }
    }

    private Mono<TradeSignatureResponse> saveTradeSignature(TradeSignatureRequest request) {
        return tradeSignatureRepositoryClient.save(TradeSignatureMapper.INSTANCE.toTradeSignature(request))
                .flatMap(tradeSignature ->
                        Flux.fromIterable(request.getSigners())
                                .flatMap(tradeSignerRequest -> tradeSignerRepositoryClient.save(
                                        TradeSignerMapper.INSTANCE.toTradeSigner(tradeSignerRequest)
                                ))
                                .then(createResponse(tradeSignature.getTradeSignatureId()))
                );
    }

    private Mono<TradeSignatureResponse> findTradeSignatureById(String entity, Integer originId, String productId) {
        return tradeSignatureRepositoryClient.findByParams(entity, originId, productId)
                .flatMap(tradeSignature -> createResponse(tradeSignature.getTradeSignatureId()))
                .switchIfEmpty(Mono.empty());
    }

    private Mono<TradeSignatureResponse> updateTradeSignature(Integer tradeSignatureId, TradeSignatureRequest request) {
        List<TradeSignerRequest> incoming = request.getSigners();
        // 1. Obtener TradeSigners por TradeSignatureId
        return tradeSignerRepositoryClient.findTradeSignersByTradeSignatureId(tradeSignatureId)
                .flatMap(existing -> sincronizarTradeSigners(tradeSignatureId, incoming, existing)
                        .then(tradeSignatureRepositoryClient.update(
                                tradeSignatureId, TradeSignatureMapper.INSTANCE.toTradeSignature(request)))
                        .flatMap(tradeSignature -> createResponse(tradeSignature.getTradeSignatureId()))
                );
    }

    private Mono<Void> sincronizarTradeSigners(Integer tradeSignatureId, List<TradeSignerRequest> incoming, List<TradeSigner> existing) {
        Set<String> existingIds = existing.stream().map(TradeSigner::getSignerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> incomingIds = incoming.stream().map(TradeSignerRequest::getSignerId).filter(Objects::nonNull).collect(Collectors.toSet());

        return Flux.concat(
                deleteRemovedTradeSigners(existing, incomingIds),
                insertNewTradeSigners(tradeSignatureId, incoming),
                updateExistingTradeSigners(tradeSignatureId, incoming, existingIds)
        ).then();
    }

    private Flux<Void> deleteRemovedTradeSigners(List<TradeSigner> existing, Set<String> incomingIds) {
        return Flux.fromIterable(
                existing.stream()
                        .filter(tradeSigner -> !incomingIds.contains(tradeSigner.getSignerId()))
                        .map(TradeSigner::getTradeSignerId)
                        .map(tradeSignerRepositoryClient::delete)
                        .collect(Collectors.toList())
        ).flatMap(mono -> mono);
    }

    private Flux<Void> insertNewTradeSigners(Integer tradeSignatureId, List<TradeSignerRequest> incoming) {
        return Flux.fromIterable(
                incoming.stream()
                        .filter(tradeSignerRequest -> tradeSignerRequest.getSignerId() == null)
                        .map(tradeSignerRequest -> {
                            TradeSigner tradeSignerToSave = TradeSignerMapper.INSTANCE.toTradeSigner(tradeSignerRequest);
                            tradeSignerToSave.setTradeSignatureId(tradeSignatureId);
                            return tradeSignerRepositoryClient.save(tradeSignerToSave);
                        })
                        .collect(Collectors.toList())
        ).flatMap(Mono::then);
    }

    private Publisher<Void> updateExistingTradeSigners(Integer tradeSignatureId, List<TradeSignerRequest> incoming, Set<String> existingIds) {
        return Flux.fromIterable(
                incoming.stream()
                        .filter(tradeSignerRequest -> tradeSignerRequest.getSignerId() != null && existingIds.contains(tradeSignerRequest.getSignerId()))
                        .map(tradeSignerRequest -> {
                            TradeSigner tradeSignerToSave = TradeSignerMapper.INSTANCE.toTradeSigner(tradeSignerRequest);
                            tradeSignerToSave.setTradeSignatureId(tradeSignatureId);
                            return tradeSignerRepositoryClient.save(tradeSignerToSave);
                        })
                        .collect(Collectors.toList())
        ).flatMap(Mono::then);
    }

    private Mono<TradeSignatureResponse> createResponse(Integer tradeSignatureId) {
        return Mono.just(TradeSignatureResponse.builder().tradeSignatureId(
                tradeSignatureId).build());
    }

}

/**
 * Excepción personalizada para errores de negocio en firma digital.
 */
class DigitalSignatureBusinessException extends RuntimeException {
    public DigitalSignatureBusinessException(String message) {
        super(message);
    }
}


CREATE TABLE ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS (
    ID                  NUMBER(22 CHAR) GENERATED BY DEFAULT AS IDENTITY,
    ENTITY              VARCHAR2(4 CHAR) NOT NULL,
    PRODUCT             VARCHAR2(2 CHAR) NOT NULL,
    DOCUMENT_TYPE       VARCHAR2(2 CHAR) NOT NULL,
    IS_PRECONTRACTUAL   VARCHAR2(1 CHAR) NOT NULL,
    DOCUMENTAL_TYPE_DOC VARCHAR2(20 CHAR) NOT NULL,
    DOCUMENTAL_CODE_DOC VARCHAR2(2 CHAR) NOT NULL,
    FECALTA             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    USUALTA             VARCHAR2(30 CHAR) NOT NULL,
    FECMODI             TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    USUMODI             VARCHAR2(30 CHAR) NOT NULL,
    CONSTRAINT PK_FX_PRODUCT_DOCUMENT_PARAMETERS PRIMARY KEY (ID)
) TABLESPACE ACELER_FX_DATOS;

COMMENT ON TABLE ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS IS 'Defines document parameters related to FX.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.ID IS 'Unique identifier of the document parameter.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.ENTITY IS 'Entity code.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.PRODUCT IS 'Product code.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.DOCUMENT_TYPE IS 'Document type code.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.IS_PRECONTRACTUAL IS 'Indicates if the document is precontractual.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.DOCUMENTAL_TYPE_DOC IS 'Documental type.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.DOCUMENTAL_CODE_DOC IS 'Documental code.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.FECALTA IS 'Document parameters registration date.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.USUALTA IS 'Document parameters registration user.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.FECMODI IS 'Document parameters modification date.';
COMMENT ON COLUMN ACELER_FX.FX_PRODUCT_DOCUMENT_PARAMETERS.USUMODI IS 'Document parameters modification user.';
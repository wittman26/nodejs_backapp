package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureMapper;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignerMapper;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureService;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateDocumentRequest;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.CreateDocumentResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignatureResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureServiceImpl implements TradeSignatureService {

    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    private static final String ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE = "Se espera incluir originId o transferId pero no ambos";
    private static final String ERROR_MESSAGE_NO_TRANSFER_ID_FOUND = "No se ha encontrado el Transfer Id Proporcionado";

    @Override
    public Mono<CreateDocumentResponse> createDocument(String originId, Locale locale, String entity, CreateDocumentRequest request) {
        return Mono.empty();
    }

    /**
     * Crea o actualiza una firma de operación.
     */
    @Override
    public Mono<TradeSignatureResponse> createOrUpdateSignature(Locale locale, String entity, TradeSignatureRequest request) {
        boolean hasTradeSignatureId = request.getTradeSignatureId() != null;
        boolean hasOriginId = request.getOriginId() != null;

        validateCreateOrUpdateParams(hasTradeSignatureId, hasOriginId);

        // Si es actualización, buscar y actualizar; si no, crear nueva
        return findTradeSignature(request, entity)
                .flatMap(tradeSignatureFound -> updateTradeSignature(tradeSignatureFound, request, entity))// Logica de actualizar
                .switchIfEmpty(Mono.defer(() -> saveTradeSignature(request, entity))); // Logica de salvar
    }

    /**
     * Valida los parámetros de entrada para crear o actualizar una firma.
     */
    private void validateCreateOrUpdateParams(boolean hasTradeSignatureId, boolean hasOriginId) {
        if (hasTradeSignatureId == hasOriginId) {
            log.error(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
            throw new IllegalArgumentException(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
        }
    }

    private Mono<TradeSignature> findTradeSignature(TradeSignatureRequest request, String entity) {
        // Prepara los filtros
        TradeSignatureFindRequest filters = TradeSignatureFindRequest.builder()
                .tradeSignatureId(request.getTradeSignatureId())
                .entity(entity)
                .originId(request.getOriginId())
                .productId(request.getProductId())
                .build();

        return tradeSignatureRepositoryClient.find(filters);
    }

    private Mono<TradeSignatureResponse> updateTradeSignature(TradeSignature tradeSignatureFound, TradeSignatureRequest request, String entity) {
        TradeSignature tradeSignature = prepareTradeSignature(request,entity);
        // Si es actualización, conserva el ID, validatedBo, OriginID
        tradeSignature.setValidatedBo(tradeSignatureFound.getValidatedBo());
        tradeSignature.setOriginId(tradeSignatureFound.getOriginId());
        tradeSignature.setTradeSignatureId(tradeSignatureFound.getTradeSignatureId());

        return persistTradeSignature(tradeSignature);
    }

    private Mono<TradeSignatureResponse> saveTradeSignature(TradeSignatureRequest request, String entity) {
        TradeSignature tradeSignature = prepareTradeSignature(request,entity);

        if (tradeSignature.getOriginId() == null) {
            throw new IllegalArgumentException(ERROR_MESSAGE_NO_TRANSFER_ID_FOUND);
        }
        return persistTradeSignature(tradeSignature);
    }

    private TradeSignature prepareTradeSignature(TradeSignatureRequest request, String entity) {
        TradeSignature tradeSignature = TradeSignatureMapper.INSTANCE.toTradeSignature(request);
        tradeSignature.setEntity(entity);
        tradeSignature.setValidatedBo("PENDING");
        tradeSignature.setOrigin(isEventProduct(tradeSignature.getProductId()) ? "EVENT" : "TRADE");
        tradeSignature.setTradeSignerList(
                request.getSigners().stream()
                        .map(TradeSignerMapper.INSTANCE::toTradeSigner)
                        .toList()
        );

        return tradeSignature;
    }

    private Mono<TradeSignatureResponse> persistTradeSignature(TradeSignature tradeSignature) {
        // Si la lista de signers es nula, usa una lista vacía
        if (tradeSignature.getTradeSignerList() == null) {
            tradeSignature.setTradeSignerList(List.of());
        } else {
            // Asegura que cada signer tenga el tradeSignatureId correcto
            tradeSignature.getTradeSignerList().forEach(
                    signer -> signer.setTradeSignatureId(tradeSignature.getTradeSignatureId())
            );
        }

        return tradeSignatureRepositoryClient.save(tradeSignature)
                .flatMap(saved -> createResponse(saved.getTradeSignatureId()));
    }

    private Mono<TradeSignatureResponse> createResponse(Long tradeSignatureId) {
        return Mono.just(TradeSignatureResponse.builder().tradeSignatureId(
                tradeSignatureId.intValue()).build());
    }

    private boolean isEventProduct(String productId) {
        return Arrays.asList("AN", "IN", "PC", "PS").contains(productId);
    }

}
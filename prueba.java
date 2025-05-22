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

    @Override
    public Mono<TradeSignatureResponse> createOrUpdateSignature(Locale locale, String entity, TradeSignatureRequest request) {
        var hasTradeSignatureId = request.getTradeSignatureId() != null;
        var hasOriginId = request.getOriginId() != null;

        if(hasTradeSignatureId == hasOriginId) {
            log.error(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
            throw new IllegalArgumentException(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
        }

        if(hasTradeSignatureId) {
            // Lógica de Actualización
            return updateTradeSignature(request.getTradeSignatureId(), request);
        } else {
            // Consultar FX_TRADE_SIGNATURE, campo TRADE_SIGNATURE_ID
            return findTradeSignatureById(entity, request.getOriginId(), request.getProductId())
                    .flatMap(tradeSignatureResponse -> updateTradeSignature(tradeSignatureResponse.getTradeSignatureId(), request)) // Lógica de Actualización
                    .switchIfEmpty(saveTradeSignature(request)); // Lógica de Alta
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

        var incoming = request.getSigners();

        // 1. Obtener TradeSigners por TradeSignatureId
        return tradeSignerRepositoryClient.findTradeSignersByTradeSignatureId(tradeSignatureId)
                .flatMap(existing -> sycronizarTradeSigners(tradeSignatureId, incoming, existing)
                .then(tradeSignatureRepositoryClient.update(
                        tradeSignatureId, TradeSignatureMapper.INSTANCE.toTradeSignature(request)))
                .flatMap(tradeSignature -> createResponse(tradeSignature.getTradeSignatureId()))
        );

    }

    private Mono<Void> sycronizarTradeSigners(Integer tradeSignatureId, List<TradeSignerRequest> incoming, List<TradeSigner> existing) {
        var existingIds = existing.stream().map(TradeSigner::getSignerId).filter(Objects::nonNull).collect(Collectors.toSet());
        var incomingIds = incoming.stream().map(TradeSignerRequest::getSignerId).filter(Objects::nonNull).collect(Collectors.toSet());

        return Flux.concat(
                deleteRemovedTradeSigners(existing, incomingIds),
                insertNewTradeSigners(tradeSignatureId, incoming),
                updateExitingTradeSigners(tradeSignatureId, incoming, existingIds)
        ).then();
    }

    private Flux<Void> deleteRemovedTradeSigners(List<TradeSigner> existing, Set<String> incomingIds) {
        return Flux.fromIterable(
                existing.stream()
                        .filter(tradeSigner -> !incomingIds.contains(tradeSigner.getSignerId()) )
                        .map(TradeSigner::getTradeSignerId)
                        .map(tradeSignerRepositoryClient::delete)
                        .toList()
        ).flatMap(mono -> mono);
    }

    private Flux<Void> insertNewTradeSigners(Integer tradeSignatureId, List<TradeSignerRequest> incoming) {
        return Flux.fromIterable(
                incoming.stream()
                        .filter(tradeSignerRequest -> tradeSignerRequest.getSignerId() == null)
                        .map(tradeSignerRequest -> {
                            var tradeSignerToSave = TradeSignerMapper.INSTANCE.toTradeSigner(tradeSignerRequest);
                            tradeSignerToSave.setTradeSignatureId(tradeSignatureId);
                            return tradeSignerRepositoryClient.save(tradeSignerToSave);
                        })
                        .toList()
        ).flatMap(Mono::then);
    }

    private Publisher<Void> updateExitingTradeSigners(Integer tradeSignatureId, List<TradeSignerRequest> incoming, Set<String> existingIds) {
        return Flux.fromIterable(
                incoming.stream()
                        .filter(tradeSignerRequest -> tradeSignerRequest.getSignerId() != null && existingIds.contains(tradeSignerRequest.getSignerId()))
                        .map(tradeSignerRequest -> {
                            var tradeSignerToSave = TradeSignerMapper.INSTANCE.toTradeSigner(tradeSignerRequest);
                            tradeSignerToSave.setTradeSignatureId(tradeSignatureId);
                            return tradeSignerRepositoryClient.save(tradeSignerToSave);
                        })
                        .toList()
        ).flatMap(Mono::then);
    }

    private Mono<TradeSignatureResponse> createResponse(Integer tradeSignatureId) {
        return Mono.just(TradeSignatureResponse.builder().tradeSignatureId(
                tradeSignatureId).build());
    }

}

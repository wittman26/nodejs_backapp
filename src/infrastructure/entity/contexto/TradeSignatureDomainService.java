package com.acelera.fx.digitalsignature.domain;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.fx.db.domain.port.TradeSignatureViewRepositoryClient;
import com.acelera.fx.digitalsignature.application.service.mapper.*;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.infrastructure.request.GetTradeSignatureRequestParameter;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.GetTradeSignatureResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignatureResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignersResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureDomainService {
    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;
    private final TradeSignatureViewRepositoryClient tradeSignatureViewRepositoryClient;
    private final TradeSignerHelper tradeSignerHelper;

    public void validateCreateOrUpdateParams(TradeSignatureRequest request) {
        boolean hasTradeSignatureId = request.getTradeSignatureId() != null;
        boolean hasOriginId = request.getOriginId() != null;

        validateTradeSignatureIdOriginId(hasTradeSignatureId, hasOriginId);
    }

    public void validateTradeSignatureIdOriginId(boolean hasTradeSignatureId, boolean hasOriginId) {
        if (hasTradeSignatureId == hasOriginId) {
            log.error(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
            throw new IllegalArgumentException(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
        }
    }

    public void validateGetParams(GetTradeSignatureRequestParameter request) {
        // Lógica de validación aquí
        boolean hasTradeSignatureId = request.getTradeSignatureId() != null;
        boolean hasOriginId = request.getOriginId() != null;
        boolean hasOrigin = request.getOrigin() != null;

        validateTradeSignatureIdOriginId(hasTradeSignatureId, hasOriginId);

        if (hasOriginId != hasOrigin) {
            log.error(ERROR_MESSAGE_DIGITAL_SIGNATURE_GET_SIGNATURE);
            throw new IllegalArgumentException(ERROR_MESSAGE_DIGITAL_SIGNATURE_GET_SIGNATURE);
        }
    }

    public Mono<TradeSignature> findTradeSignature(TradeSignatureRequest request, String entity) {
        // Lógica de búsqueda
        // Prepara los filtros
        TradeSignatureFindRequest filters = TradeSignatureRequestMapper.INSTANCE.toTradeSignatureFindRequest(request);
        filters.setEntity(entity);

        return tradeSignatureRepositoryClient.find(filters);
    }

    public Mono<TradeSignatureResponse> upsertTradeSignature(TradeSignature tradeSignatureFound, TradeSignatureRequest request, String entity) {
        // Lógica de actualización/creación
        TradeSignature tradeSignature = TradeSignatureMapper.INSTANCE.toTradeSignature(request);
        tradeSignature.setEntity(entity);
        tradeSignature.setValidatedBo(VALIDATED_BO_DEFAULT);
        tradeSignature.setOrigin(isEventProduct(tradeSignature.getProductId()) ? ORIGIN_EVENT : ORIGIN_TRADE);
        tradeSignature.setTradeSignerList(request.getSigners().stream().map(TradeSignerMapper.INSTANCE::toTradeSigner).toList());

        // Si es actualización, conserva el ID, validatedBo, OriginID y limpia la lista anterior
        if (tradeSignatureFound != null) {
            tradeSignature.setValidatedBo(tradeSignatureFound.getValidatedBo());
            tradeSignature.setOriginId(tradeSignatureFound.getOriginId());
            tradeSignature.setTradeSignatureId(tradeSignatureFound.getTradeSignatureId());
        }

        // Si la lista de signers es nula, usa una lista vacía
        tradeSignature.setTradeSignerList(tradeSignature.getTradeSignerList());

        // Asegura que cada signer tenga el tradeSignatureId correcto
        tradeSignature.getTradeSignerList()
                .forEach(signer -> signer.setTradeSignatureId(tradeSignature.getTradeSignatureId()));

        if (tradeSignatureFound == null && tradeSignature.getOriginId() == null) {
            throw new IllegalArgumentException(ERROR_MESSAGE_NO_TRANSFER_ID_FOUND);
        }

        return tradeSignatureRepositoryClient.save(tradeSignature)
                .flatMap(saved -> Mono.just(TradeSignatureResponse.builder()
                        .tradeSignatureId(saved.getTradeSignatureId().intValue())
                        .build())
                );
    }

    private boolean isEventProduct(String productId) {
        return Arrays.asList("AN", "IN", "PC", "PS").contains(productId);
    }

    public Mono<GetTradeSignatureResponse> getTradeSignatureResponse(Long tradeSignatureId) {
        // Build header
        Mono<GetTradeSignatureResponse> headerMono = tradeSignatureViewRepositoryClient.findTradeSignatureViewExpedient(tradeSignatureId)
                .flatMap(response -> Mono.just(TradeSignatureViewMapper.INSTANCE.toGetTradeSignatureResponse(response)))
                .switchIfEmpty(Mono.empty());

        // Build Signers
        Mono<List<TradeSignersResponse>> signersMono = tradeSignatureViewRepositoryClient.findTradeSignerViewDocument(tradeSignatureId)
                .map(this::mapSignersWithColour)
                .switchIfEmpty(Mono.empty());

        return Mono.zip(headerMono, signersMono)
                .map( tuple -> {
                    GetTradeSignatureResponse header = tuple.getT1();
                    List<TradeSignersResponse> signers = tuple.getT2();

                    header.setSigners(signers);
                    return header;
                });
    }

    public List<TradeSignersResponse> mapSignersWithColour(List<TradeSignerDocumentStatusView> views) {
        return views.stream()
                .collect(Collectors.groupingBy(TradeSignerDocumentStatusView::getSignerId))
                .values()
                .stream()
                .map(signerDocs -> {
                    TradeSignerDocumentStatusView base = signerDocs.get(0);

                    String signerColour = tradeSignerHelper.getSignerColour(signerDocs);

                    var tradeSignersResponse = TradeSignerDocumentStatusViewMapper.INSTANCE.toTradeSignersResponse(base);
                    tradeSignersResponse.setSignerColour(signerColour);
                    tradeSignersResponse.setDocs(signerDocs.stream().map(
                            TradeSignerDocumentStatusViewMapper.INSTANCE::toStatusDocumentPerSigner
                    ).toList());

                    return tradeSignersResponse;
                }).toList();
    }
}

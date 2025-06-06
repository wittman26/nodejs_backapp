package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.fx.db.domain.port.TradeSignatureViewRepositoryClient;
import com.acelera.fx.digitalsignature.application.service.mapper.*;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureService;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateDocumentRequest;
import com.acelera.fx.digitalsignature.infrastructure.request.GetTradeSignatureRequestParameter;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureServiceImpl implements TradeSignatureService {

    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    private final TradeSignatureViewRepositoryClient tradeSignatureViewRepositoryClient;

    private static final String ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE = "Se espera incluir originId o transferId pero no ambos";
    private static final String ERROR_MESSAGE_NO_TRANSFER_ID_FOUND = "No se ha encontrado el Transfer Id Proporcionado";
    private static final String ERROR_MESSAGE_DIGITAL_SIGNATURE_GET_SIGNATURE = "Se espera incluir originId y Origin si transferId no se proporciona";

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
                .flatMap(tradeSignatureFound -> upsertTradeSignature(tradeSignatureFound, request, entity)) // Logica de actualizar
                .switchIfEmpty(Mono.defer(() -> upsertTradeSignature(null, request, entity))); // Logica de salvar
    }

    @Override
    public Mono<GetTradeSignatureResponse> getTradeSignature(Locale locale, String entity,
            GetTradeSignatureRequestParameter request) {

        boolean hasTradeSignatureId = request.getTradeSignatureId() != null;
        boolean hasOriginId = request.getOriginId() != null;
        boolean hasOrigin = request.getOrigin() != null;

        validateGetParams(hasTradeSignatureId, hasOriginId, hasOrigin);

        var tradeSignatureRequest = TradeSignatureRequestMapper.INSTANCE.toTradeSignatureRequest(request);

        return findTradeSignature(tradeSignatureRequest, entity)
                .flatMap(tradeSignatureFound -> buildResponse(tradeSignatureFound.getTradeSignatureId()))
                .switchIfEmpty(buildResponse(request.getTradeSignatureId()));
    }

    private Mono<GetTradeSignatureResponse> buildResponse(Long tradeSignatureId ) {

        // Build header
        Mono<GetTradeSignatureResponse> cabeceraMono = tradeSignatureViewRepositoryClient.findTradeSignatureViewExpedient(tradeSignatureId)
                .flatMap(response -> Mono.just(TradeSignatureViewMapper.INSTANCE.toGetTradeSignatureResponse(response)))
                .switchIfEmpty(Mono.empty());

        // Build Signers
        Mono<List<TradeSignersResponse>> signersMono = tradeSignatureViewRepositoryClient.findTradeSignerViewDocument(tradeSignatureId)
                .map(this::mapSignersWithColour)
                .switchIfEmpty(Mono.empty());

        return Mono.zip(cabeceraMono, signersMono)
                .map( tuple -> {
                    GetTradeSignatureResponse cabecera = tuple.getT1();
                    List<TradeSignersResponse> detalle = tuple.getT2();

                    cabecera.setSigners(detalle);
                    return cabecera;
                });
    }

    public List<TradeSignersResponse> mapSignersWithColour(List<TradeSignerDocumentStatusView> views) {
        return views.stream()
                .collect(Collectors.groupingBy(TradeSignerDocumentStatusView::getSignerId))
                .values()
                .stream()
                .map(signerDocs -> {
                    TradeSignerDocumentStatusView base = signerDocs.get(0);

                    String signerColour = getSignerColour(signerDocs);

                    var resultado = TradeSignerDocumentStatusViewMapper.INSTANCE.toTradeSignersResponse(base);
                    resultado.setSignerColour(signerColour);
                    resultado.setDocs(signerDocs.stream().map(this::mapearDoc).toList());
                    return resultado;
                }).toList();
    }

    private StatusDocumentPerSigner mapearDoc(TradeSignerDocumentStatusView doc) {
        // TODO
        return StatusDocumentPerSigner.builder().build();
    }

    private String getSignerColour(List<TradeSignerDocumentStatusView> signerDocs) {
        boolean allSigned = signerDocs.stream().allMatch(doc -> "Y".equals(doc.getSignedDoc()));
        boolean allNotSigned = signerDocs.stream().allMatch(doc -> "N".equals(doc.getSignedDoc()));

        if (allSigned) return "GREEN";
        if (allNotSigned) return "RED";
        return "YELLOW";
    }

    /**
     * Valida los parámetros de entrada para obtener una firma.
     */
    private void validateGetParams(boolean hasTradeSignatureId, boolean hasOriginId, boolean hasOrigin) {
        validateCreateOrUpdateParams(hasTradeSignatureId, hasOriginId);
        if (hasOriginId != hasOrigin) {
            log.error(ERROR_MESSAGE_DIGITAL_SIGNATURE_GET_SIGNATURE);
            throw new IllegalArgumentException(ERROR_MESSAGE_DIGITAL_SIGNATURE_GET_SIGNATURE);
        }
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
        TradeSignatureFindRequest filters = TradeSignatureRequestMapper.INSTANCE.toTradeSignatureFindRequest(request);
        filters.setEntity(entity);

        return tradeSignatureRepositoryClient.find(filters);
    }

    /**
     * Crea o actualiza la entidad TradeSignature y sus hijos.
     */
    private Mono<TradeSignatureResponse> upsertTradeSignature(TradeSignature tradeSignatureFound, TradeSignatureRequest request, String entity) {
        TradeSignature tradeSignature = TradeSignatureMapper.INSTANCE.toTradeSignature(request);
        tradeSignature.setEntity(entity);
        tradeSignature.setValidatedBo("PENDING");
        tradeSignature.setOrigin(isEventProduct(tradeSignature.getProductId()) ? "EVENT" : "TRADE");
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

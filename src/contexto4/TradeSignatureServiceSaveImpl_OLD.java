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

    private GetTradeSignatureParameterDto getSignerRequest(Long tradeSignatureId, Long originId, String origin) {
        return GetTradeSignatureParameterDto.builder()
                .tradeSignatureId(tradeSignatureId)
                .origin(origin)
                .originId(originId)
                .build();
    }



    @Override
    public Mono<CreateExpedientResponse> createSignatureExpedient(Locale locale, String entity, Long originId, CreateExpedientRequest request) {
        // 0: Determinar origen (TRADE o EVENT) según productId
        String origin = tradeSignerHelper.isEventProduct(request.getProductId()) ? "EVENT" : "TRADE";

        // 1: Obtener tradeSignatureId (ya encapsula toda la lógica interna)
        return tradeSignatureServiceGet.getTradeSignature(entity, originId, request)
                .switchIfEmpty(Mono.error(new RuntimeException("tradeSignature Not found.")))
                .flatMap(tradeSignature -> {
                    log.info("1: Obtener tradeSignatureId : {}", tradeSignature.getTradeSignatureId());
                    // 2: Obtener firmantes (signers)
                    var signerRequest = getSignerRequest(tradeSignature.getTradeSignatureId(), originId, origin);
                    return tradeSignatureServiceGet.getTradeSignature(locale, entity, signerRequest)
                            .map(GetTradeSignatureDto::getSigners)
                            .flatMap(signers -> {
                                        log.info("2: Obtener firmantes (signers): {} ", signers.size() );
                                        signers.forEach( signer ->
                                                log.info("Signer: {}", signer.getSignerId())
                                        );
                                        // 3: Obtener tipos de documentos por producto
                                        return productDocumentsService.findProductDocumentType(entity, locale, request.getProductId())
                                                .collectList()
                                                .flatMap(documentTypes -> {
                                                    log.info("3: Obtener tipos de documentos por producto: {} ", documentTypes.size());
                                                    documentTypes.forEach( documentType ->
                                                            log.info("documentType: {} - {}", documentType.getProduct(), documentType.getDocumentType())
                                                    );
                                                    return Mono.just(CreateExpedientResponse.builder().expedientId(123L).build());
                                                });
                                    }
                            );
                });
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
}

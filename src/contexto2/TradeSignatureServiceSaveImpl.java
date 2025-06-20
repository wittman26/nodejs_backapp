package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureMapper;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureRequestMapper;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceSave;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;

import static com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper.ERROR_HAS_EXPEDIENT_ID;
import static com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper.ERROR_MESSAGE_NO_TRANSFER_ID_FOUND;
import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureServiceSaveImpl implements TradeSignatureServiceSave {

    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    @Override
    public Mono<TradeSignature> createOrUpdateSignature(Locale locale, String entity, TradeSignatureDto dto) {
        // Si es actualización, buscar y actualizar; si no, crear nueva
        return tradeSignatureRepositoryClient.find(TradeSignatureRequestMapper.INSTANCE.fromDtoToTradeSignatureFindRequest(dto, entity))
                .flatMap(tradeSignatureFound -> upsertTradeSignature(tradeSignatureFound, dto, entity)) // Logica de actualizar
                .switchIfEmpty(Mono.defer(() -> upsertTradeSignature(null, dto, entity))); // Logica de salvar
    }

    public Mono<TradeSignature> upsertTradeSignature(TradeSignature tradeSignatureFound, TradeSignatureDto dto, String entity) {
        // Lógica de actualización/creación
        TradeSignature tradeSignature = TradeSignatureMapper.INSTANCE.fromDtoToTradeSignature(dto, entity);

        // Si es actualización, conserva el ID, validatedBo, OriginID y limpia la lista anterior
        if (tradeSignatureFound != null) {
            if(tradeSignatureFound.getExpedientId() != null) {
                return Mono.error(CustomErrorException.ofArguments(BAD_REQUEST, ERROR_HAS_EXPEDIENT_ID+ tradeSignatureFound.getExpedientId()));
            }
            tradeSignature.setValidatedBo(tradeSignatureFound.getValidatedBo());
            tradeSignature.setOriginId(tradeSignatureFound.getOriginId());
            tradeSignature.setTradeSignatureId(tradeSignatureFound.getTradeSignatureId());
        } else {
            if(tradeSignature.getOriginId()==null) {
                return Mono.error(CustomErrorException.ofArguments(NOT_FOUND, ERROR_MESSAGE_NO_TRANSFER_ID_FOUND));
            }
        }

        tradeSignature.setTradeSignerList(tradeSignature.getTradeSignerList());

        // Asegura que cada signer tenga el tradeSignatureId correcto
        tradeSignature.getTradeSignerList()
                .forEach(signer -> signer.setTradeSignatureId(tradeSignature.getTradeSignatureId()));

        return tradeSignatureRepositoryClient.save(tradeSignature);
    }
}


package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureViewFindRequest;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.fx.db.domain.port.TradeSignatureViewRepositoryClient;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureMapper;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignerMapper;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureService;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateDocumentRequest;
import com.acelera.fx.digitalsignature.infrastructure.request.GetTradeSignatureRequestParameter;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.CreateDocumentResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.GetTradeSignatureResponse;
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

    private final TradeSignatureViewRepositoryClient tradeSignatureViewRepositoryClient;

    @Override
    public Mono<GetTradeSignatureResponse> getTradeSignature(Locale locale, String entity,
            GetTradeSignatureRequestParameter request) {

        TradeSignatureViewFindRequest filters = TradeSignatureViewFindRequest.builder()
                .entity(entity)
                .originId(request.getOriginId())
                .origin(request.getOrigin())
                .tradeSignatureId(request.getTradeSignatureId())
                .build();

        Mono<GetTradeSignatureResponse> cabeceraMono = tradeSignatureViewRepositoryClient.findTradeSignatureViewExpedient(filters)
            .map(tradeSignatureExpedientView -> GetTradeSignatureResponse.builder()
                .tradeSignatureId(tradeSignatureExpedientView.getTradeSignatureId())
                .entity(tradeSignatureExpedientView.getEntity())
                // Agrega aquí otros campos de cabecera si los tienes en tu DTO
                .build()
            )
            .switchIfEmpty(Mono.just(GetTradeSignatureResponse.builder().build()));

        Mono<List<TradeSignerResponse>> signersMono = tradeSignatureViewRepositoryClient.findTradeSignerViewDocument(filters)
            .map(list -> list.stream()
                .map(TradeSignerMapper.INSTANCE::toTradeSignerResponse)
                .toList()
            )
            .switchIfEmpty(Mono.just(List.of()));

        return Mono.zip(cabeceraMono, signersMono)
            .map(tuple -> {
                GetTradeSignatureResponse cabecera = tuple.getT1();
                List<TradeSignerResponse> signers = tuple.getT2();
                return GetTradeSignatureResponse.builder()
                    .tradeSignatureId(cabecera.getTradeSignatureId())
                    .entity(cabecera.getEntity())
                    // Agrega aquí otros campos de cabecera si los tienes
                    .signers(signers)
                    .build();
            });
    }

}



expedient → Solo si FX_VIEW_TRADE_SIGNATURE_EXPEDIENT.EXPEDIENT_ID es distinto de null
    expedientId → FX_VIEW_TRADE_SIGNATURE_EXPEDIENT.EXPEDIENT_ID
    isActive → Si FX_VIEW_TRADE_SIGNATURE_EXPEDIENT.END_DATE >= HOY, isActive = true; else isActive = false
    startDate→ FX_VIEW_TRADE_SIGNATURE_EXPEDIENT.START_DATE formato ‘dd/mm/aaaa hh:mm:ss' y pasada a hora local, en BBDD está salvo error en hora UTC
    endDate → FX_VIEW_TRADE_SIGNATURE_EXPEDIENT.END_DATE formato ‘dd/mm/aaaa hh:mm:ss' y pasada a hora local, en BBDD está salvo error en hora UTC
    status → FX_VIEW_TRADE_SIGNATURE_EXPEDIENT.EXPEDIENT_STATUS
    statusDescription → Si FX_VIEW_TRADE_SIGNATURE_EXPEDIENT.EXPEDIENT_STATUS = ‘PENDING’, statusDescription = ‘Pte. firma’; si EXPEDIENT_STATUS = ‘COMPLETED’, statusDescription = ‘Firmada'; si EXPEDIENT_STATUS = ‘CANCELLED’, statusDescription = ‘Cancelada'; else statusDescription = null
    hasClauses → Si FX_VIEW_TRADE_SIGNATURE_EXPEDIENT.HAS_CLAUSES = 'Y', hasClauses = true; else hasClauses = false
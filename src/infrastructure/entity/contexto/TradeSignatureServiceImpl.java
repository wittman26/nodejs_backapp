
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

        var responseFinal = GetTradeSignatureResponse.builder().build();

        return tradeSignatureViewRepositoryClient.findTradeSignerViewDocument(filters)
                .flatMap(response -> {
                    //TODO
                })
                .switchIfEmpty(Mono.empty());

}
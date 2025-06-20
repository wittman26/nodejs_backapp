package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedientFindByFilterRequest;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.fx.db.domain.port.ViewTradeSignatureRepositoryClient;
import com.acelera.broker.shared.domain.PageDto;
import com.acelera.broker.shared.domain.PageableDto;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureRequestMapper;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureViewMapper;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignerDocumentStatusViewMapper;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureParameterDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceGet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureServiceGetImpl implements TradeSignatureServiceGet {

    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    private final ViewTradeSignatureRepositoryClient viewTradeSignatureRepositoryClient;

    private final TradeSignerHelper tradeSignerHelper;

    @Override
    public Mono<GetTradeSignatureDto> getTradeSignature(Locale locale, String entity,
            GetTradeSignatureParameterDto dto) {

        return tradeSignatureRepositoryClient.find(
                TradeSignatureRequestMapper.INSTANCE.fromGetDtoToTradeSignatureFindRequest(dto, entity))
                .flatMap(tradeSignatureFound -> getTradeSignatureResponse(tradeSignatureFound.getTradeSignatureId()))
                .switchIfEmpty(getTradeSignatureResponse(dto.getTradeSignatureId()));
    }

    public Mono<GetTradeSignatureDto> getTradeSignatureResponse(Long tradeSignatureId) {
        // Build header
        var filterRequest = ViewTradeSignatureExpedientFindByFilterRequest.builder()
                .tradeSignatureId(tradeSignatureId).pageable(PageableDto.defaultPageable()).build();
        var headerMono = viewTradeSignatureRepositoryClient.findByFilter(filterRequest)
                .map(PageDto::getContent).flatMapMany(Flux::fromIterable)
                .single()
                .flatMap(response -> Mono.just(TradeSignatureViewMapper.INSTANCE.fromDataToGetTradeSignatureDto(response)))
                .switchIfEmpty(Mono.empty());

        // Build Signers
        Mono<List<TradeSignerDto>> signersMono = viewTradeSignatureRepositoryClient.findTradeSignerViewDocument(tradeSignatureId)
                .map(this::mapSignersWithColour)
                .switchIfEmpty(Mono.empty());

        return Mono.zip(headerMono, signersMono)
                .map( tuple -> {
                    GetTradeSignatureDto header = tuple.getT1();
                    List<TradeSignerDto> signers = tuple.getT2();

                    header.setSigners(signers);
                    return header;
                });
    }

    public List<TradeSignerDto> mapSignersWithColour(List<TradeSignerDocumentStatusView> views) {
        return views.stream()
                .collect(Collectors.groupingBy(TradeSignerDocumentStatusView::getSignerId))
                .values()
                .stream()
                .map(signerDocs -> {
                    TradeSignerDocumentStatusView base = signerDocs.getFirst();

                    String signerColour = tradeSignerHelper.getSignerColour(signerDocs);

                    var tradeSignersResponse = TradeSignerDocumentStatusViewMapper.INSTANCE.fromDatatoTradeSignerDto(base);
                    tradeSignersResponse.setSignerColour(signerColour);
                    tradeSignersResponse.setDocs(signerDocs.stream().map(
                            TradeSignerDocumentStatusViewMapper.INSTANCE::fromDataToStatusDocumentPerSignerDto
                    ).toList());

                    return tradeSignersResponse;
                }).toList();
    }
}

package com.acelera.rest.fx.infrastructure;

import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.request.TradeSignatureRequest;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.TradeSignatureResponse;
import com.acelera.rest.fx.domain.port.FxCaller;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
public class FxCallerImpl implements FxCaller {

    private final WebClient webClient;

    @Override
    public Mono<TradeSignatureResponse> updateTradeSignature(Locale locale, String entity,
            TradeSignatureRequest request) {
        return webClient.put()
                // @formatter:off
                .uri("/v1/trades-signatures")
                .bodyValue(request)
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(getCleanToken(request.getToken()));
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .retrieve()
                .bodyToMono(TradeSignatureResponse.class)
                .onErrorMap(Exception.class, e -> {
                    log.error("An error occurred while trying to UPDATE Trade Signature.: {} - {}",
                            e.getClass().getSimpleName(), e.getMessage(), e);
                    return e;
                });
        // @formatter:on
    }

    @Override
    public Mono<GetTradeSignatureResponse> getTradeSignature(Locale locale, String entity,
            GetTradeSignatureRequestParameter request) {
        return webClient.get()
                // @formatter:off
                .uri(uriBuilder -> uriBuilder.path("/v1/trades-signatures/view")
                        .queryParam("originId", Optional.ofNullable(request.getOriginId()))
                        .queryParam("origin", Optional.ofNullable(request.getOrigin()))
                        .queryParam("tradeSignatureId", Optional.ofNullable(request.getTradeSignatureId()))
                        .build())
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(getCleanToken(request.getToken()));
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .retrieve()
                .bodyToMono(GetTradeSignatureResponse.class)
                .onErrorMap(Exception.class, e -> {
                    log.error("An error occurred while trying to GET Trade Signature.: {} - {}",
                            e.getClass().getSimpleName(), e.getMessage(), e);
                    return e;
                });
        // @formatter:on
    }

    private String getCleanToken(String token) {
        return token.replaceFirst("(?i)^Bearer","");
    }
}

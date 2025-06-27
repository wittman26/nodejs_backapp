package com.acelera.rest.fx.infrastructure;

import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.request.TradeSignatureRequest;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.TradeSignatureResponse;
import com.acelera.rest.fx.domain.port.FxCaller;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException.InternalServerError;
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
        String cleanToken = request.getToken().replaceFirst("(?i)^Bearer","");
        return webClient.put()
                // @formatter:off
                .uri("/v1/trades-signatures")
                .bodyValue(request)
                .headers(httpHeaders -> {
                    httpHeaders.setBearerAuth(cleanToken);
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .retrieve()
                .bodyToMono(TradeSignatureResponse.class)
                .onErrorMap(Exception.class, e -> {
                    log.info("An error occurred while trying to get Trade Signature.: {} - {}",
                            e.getClass().getSimpleName(), e.getMessage(), e);
                    return e;
                    })
                .onErrorMap(InternalServerError.class, this::extractError);
        // @formatter:on
    }

    private Throwable extractError(InternalServerError e) {
        JsonNode node;
        try {
            node = new ObjectMapper().readTree(e.getResponseBodyAsString());
        } catch (JsonProcessingException ex) {
            log.info("Warn extracting error itqef", ex);
            return e;
        }

        var error = node.findValue("ERROR_MESSAGE").asText();
        return StringUtils.isBlank(error) ? e : new IllegalArgumentException(error);
    }

    @Override
    public Mono<GetTradeSignatureResponse> getTradeSignature(Locale locale, String entity,
            GetTradeSignatureRequestParameter request) {
        String cleanToken = request.getToken().replaceFirst("(?i)^Bearer","");
        return webClient.get()
// @formatter:off
                .uri(uriBuilder -> uriBuilder.path("/v1/trades-signatures/view")
                        .queryParam("originId", Optional.ofNullable(request.getOriginId()))
                        .queryParam("origin", Optional.ofNullable(request.getOrigin()))
                        .queryParam("tradeSignatureId", Optional.ofNullable(request.getTradeSignatureId()))
                        .build())
                        .headers(httpHeaders -> {
                            httpHeaders.setBearerAuth(cleanToken);
                            httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                        })
                .retrieve()
                .bodyToMono(GetTradeSignatureResponse.class);
// @formatter:on
    }
}

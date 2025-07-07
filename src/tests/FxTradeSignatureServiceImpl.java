package com.acelera.integration.rest.application.service;

import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.request.TradeRequestContext;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.port.TradeSignatureClient;
import com.acelera.error.CustomErrorException;
import com.acelera.integration.rest.domain.port.service.FxTradeSignatureService;
import io.rsocket.exceptions.ApplicationErrorException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class FxTradeSignatureServiceImpl implements FxTradeSignatureService {
    private final TradeSignatureClient client;

    @Override
    public Mono<GetTradeSignatureResponse> getTradeSignature(String entity, Locale locale, GetTradeSignatureRequestParameter request, ServerHttpRequest httpRequest) {

        enrichRequest(request, entity, locale, httpRequest);

        return client.getTradeSignature(request)
                .onErrorMap(ApplicationErrorException.class,
                        ex -> {
                            log.error("An error occurred while trying to get Trade Signature.: {} - {}",
                                    ex.getClass().getSimpleName(), ex.getMessage(), ex);
                            return CustomErrorException.ofArguments(HttpStatus.INTERNAL_SERVER_ERROR,ex.getMessage());
                        });
    }

    private void enrichRequest(TradeRequestContext request, String entity, Locale locale, ServerHttpRequest httpRequest) {
        request.setEntity(entity);
        request.setLocale(locale);
        request.setToken(extractToken(httpRequest));
    }

    private String extractToken(ServerHttpRequest httpRequest) {
        return httpRequest.getHeaders().getFirst("Authorization");
    }
}
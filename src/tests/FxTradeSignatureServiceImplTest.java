package com.acelera.integration.rest.application.service;

import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.port.TradeSignatureClient;
import com.acelera.error.CustomErrorException;
import com.acelera.locale.LocaleConstants;
import com.acelera.locale.MessageSourceHolder;
import io.rsocket.exceptions.ApplicationErrorException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FxTradeSignatureServiceImplTest {
    private @InjectMocks FxTradeSignatureServiceImpl impl;
    private @Mock TradeSignatureClient client;
    private @Mock ServerHttpRequest serverHttpRequest;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    @Test
    void testGetTradeSignature() {
        var response = PODAM_FACTORY.manufacturePojo(GetTradeSignatureResponse.class);

        var locale = LocaleConstants.DEFAULT_LOCALE;
        var entity = LocaleConstants.ENTITY_0049;
        var request = PODAM_FACTORY.manufacturePojo(GetTradeSignatureRequestParameter.class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer Token");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);

        when(client.getTradeSignature(any())).thenReturn(Mono.just(response));

        impl.getTradeSignature(entity, locale, request, serverHttpRequest).as(StepVerifier::create).expectNext(response)
                .verifyComplete();
    }

    @Test
    void testGetTradeSignature_whenerror() {

        var locale = LocaleConstants.DEFAULT_LOCALE;
        var entity = LocaleConstants.ENTITY_0049;
        var request = PODAM_FACTORY.manufacturePojo(GetTradeSignatureRequestParameter.class);

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer Token");
        when(serverHttpRequest.getHeaders()).thenReturn(headers);

        when(client.getTradeSignature(any())).thenReturn(Mono.error(new ApplicationErrorException("my app exception")));

        impl.getTradeSignature(entity, locale, request, serverHttpRequest).as(StepVerifier::create)
                .expectErrorSatisfies(x -> assertThat(x)
                        .isInstanceOf(CustomErrorException.class).hasMessageContaining("my app exception"))
                .verify();

    }
}
package com.acelera.integration.rest.application.service;

import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.port.TradeSignatureClient;
import com.acelera.locale.LocaleConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class FxTradeSignatureServiceImplTest {
    private @InjectMocks FxTradeSignatureServiceImpl impl;
    private @Mock TradeSignatureClient client;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    @Test
    void testGetTradeSignature() {
        var response = PODAM_FACTORY.manufacturePojo(GetTradeSignatureResponse.class);

        var locale = LocaleConstants.DEFAULT_LOCALE;
        var entity = LocaleConstants.ENTITY_0049;
        var serverHttpRequest = PODAM_FACTORY.manufacturePojo(ServerHttpRequest.class);
        var request = PODAM_FACTORY.manufacturePojo(GetTradeSignatureRequestParameter.class);

        when(client.getTradeSignature(request).thenReturn(Mono.just(response)));

        impl.getTradeSignature(entity, locale, request, serverHttpRequest).as(StepVerifier::create).expectNext(response)
                .verifyComplete();
    }
}

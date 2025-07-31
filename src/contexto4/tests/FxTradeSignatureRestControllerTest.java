package com.acelera.integration.rest.infrastructure.adapter.rest.controller;

import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.request.StartSignatureRequest;
import com.acelera.broker.fx.domain.dto.request.TradeSignatureRequest;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.StartSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.TradeSignatureResponse;
import com.acelera.error.ErrorWebFluxAutoConfig;
import com.acelera.integration.rest.domain.port.service.FxTradeSignatureService;
import com.acelera.locale.LocaleAutoConfig;
import com.acelera.locale.LocaleConstants;
import com.acelera.security.WebSecurityAutoConfig;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.Locale;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest(FxTradeSignatureRestController.class)
@Import({ LocaleAutoConfig.class, WebSecurityAutoConfig.class, ErrorWebFluxAutoConfig.class,
        ErrorWebFluxAutoConfiguration.class })
@WithMockUser(username = "x1103878")
public class FxTradeSignatureRestControllerTest {

    private @Autowired WebTestClient webClient;
    private @MockitoBean FxTradeSignatureService service;

    @Test
    void testPutTradeSignature() {
        var response = TradeSignatureResponse.builder().tradeSignatureId(9876L).build();
        var locale = ArgumentCaptor.forClass(Locale.class);
        var entity = ArgumentCaptor.forClass(String.class);
        var serverHttpRequest = ArgumentCaptor.forClass(ServerHttpRequest.class);
        var request= ArgumentCaptor.forClass(TradeSignatureRequest.class);

        var req = TradeSignatureRequest.builder().tradeSignatureId(9876L)
                .build();

        when(service.updateTradeSignature(entity.capture(), locale.capture(), request.capture(), serverHttpRequest.capture()))
                .thenReturn(Mono.just(response));

        webClient.put()
                .uri("/v1/trades-signatures")
                .bodyValue(req).accept(MediaType.APPLICATION_JSON)
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049)
                .exchange()
                .expectStatus().isOk().expectBody(TradeSignatureResponse.class)
                .value(x -> assertThat(x).usingRecursiveComparison().isEqualTo(response));
    }

    @Test
    void testGetTradeSignature() {
        var response = GetTradeSignatureResponse.builder().tradeSignatureId(9876L).build();
        var locale = ArgumentCaptor.forClass(Locale.class);
        var entity = ArgumentCaptor.forClass(String.class);
        var serverHttpRequest = ArgumentCaptor.forClass(ServerHttpRequest.class);
        var request= ArgumentCaptor.forClass(GetTradeSignatureRequestParameter.class);

        var req = GetTradeSignatureRequestParameter.builder().tradeSignatureId(9876L)
                .build();

        when(service.getTradeSignature(entity.capture(), locale.capture(), request.capture(), serverHttpRequest.capture()))
                .thenReturn(Mono.just(response));

        webClient.get()
                .uri(builder -> builder.path("/v1/trades-signatures/view")
                        //.bodyValue(getTradeSignatureRequest(ONLY_TRADE_SIGNATURE_ID)).accept(MediaType.APPLICATION_JSON)
                        .queryParam("originId", Optional.ofNullable(req.getOriginId()))
                        .queryParam("origin", Optional.ofNullable(req.getOrigin()))
                        .queryParam("tradeSignatureId", Optional.ofNullable(req.getTradeSignatureId()))
                        .build())
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049)
                .exchange()
                .expectStatus().isOk().expectBody(GetTradeSignatureResponse.class)
                .value(x -> assertThat(x).usingRecursiveComparison().isEqualTo(response));
    }

    @Test
    void testPostTradeSignature() {
        var response = StartSignatureResponse.builder().expedientId(9876L).build();
        var locale = ArgumentCaptor.forClass(Locale.class);
        var entity = ArgumentCaptor.forClass(String.class);
        var serverHttpRequest = ArgumentCaptor.forClass(ServerHttpRequest.class);
        var request= ArgumentCaptor.forClass(StartSignatureRequest.class);
        var originId = ArgumentCaptor.forClass(Long.class);

        var req = StartSignatureRequest.builder()
                .originId(123L)
                .productId("FW")
                .build();

        when(service.postStartSignatureWorkflow(entity.capture(), locale.capture(), request.capture(),
                originId.capture(), serverHttpRequest.capture()))
                .thenReturn(Mono.just(response));

        webClient.post()
                .uri(builder -> builder.path( "/v1/trades-signatures/{originId}/signatures")
                        .build(req.getOriginId())
                )
                .bodyValue(req).accept(MediaType.APPLICATION_JSON)
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049)
                .exchange()
                .expectStatus().isOk().expectBody(StartSignatureResponse.class)
                .value(x -> assertThat(x).usingRecursiveComparison().isEqualTo(response));
    }

}

package com.acelera.fx.digitalsignature.infrastructure.adapter.rest.controller;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedient;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedientFindByFilterRequest;
import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.request.StartSignatureRequest;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.StartSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.TradeSignatureResponse;
import com.acelera.broker.shared.domain.PageDto;
import com.acelera.fx.digitalsignature.application.service.ViewTradeSignatureExpedientStatusServiceImpl;
import com.acelera.fx.digitalsignature.application.usecase.port.ViewTradeSignatureFindAllUseCase;
import com.acelera.fx.digitalsignature.domain.port.dto.*;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceGet;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServicePost;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceSave;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.SignatureExpedienteStatusResponse;
import com.acelera.locale.LocaleAutoConfig;
import com.acelera.locale.LocaleConstants;
import com.acelera.security.WebSecurityAutoConfig;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

import static com.acelera.fx.digitalsignature.TestUtils.ONLY_TRADE_SIGNATURE_ID;
import static com.acelera.fx.digitalsignature.TestUtils.getTradeSignatureRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest(TradeSignatureRestController.class)
@Import({ LocaleAutoConfig.class, WebSecurityAutoConfig.class, ViewTradeSignatureExpedientStatusServiceImpl.class })
@WithMockUser(username = "x1103878")
public class TradeSignatureRestControllerTest {
    private static final String BASE_PATH = "/v1/trades-signatures";
    private static final String STATUS_PATH = BASE_PATH + "/{originId}/view/status";
    private static final String SIGNATURES_PATH = BASE_PATH + "/{originId}/signatures";
    private static final Long DEFAULT_ID = 9876L;
    private static final int PAGE_SIZE = 10;
    
    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();
    private @Autowired WebTestClient webClient;
    private @MockitoBean TradeSignatureServiceSave serviceSave;
    private @MockitoBean TradeSignatureServiceGet serviceGet;
    private @MockitoBean ViewTradeSignatureFindAllUseCase viewTradeSignatureFindAllUseCase;
    private @MockitoBean TradeSignatureServicePost tradeSignatureServicePost;

    @Nested
    class TradeSignatureEndpointTests {
        @Test
        void shouldReturnTradeSignatureResponseOk() {
            var response = createMockTradeSignatureResponse();
            setupServiceMock(response);

            preparePostRequest(BASE_PATH, getTradeSignatureRequest(ONLY_TRADE_SIGNATURE_ID))
                .exchange()
                .expectStatus().isOk()
                .expectBody(TradeSignatureResponse.class)
                .value(x -> assertThat(x)
                    .usingRecursiveComparison()
                    .isEqualTo(response));
        }
    }

    @Nested
    class SignatureExpedientStatusTests {
        @Test
        void shouldReturnStatusSuccessfully() {
            var items = Stream.generate(() -> PODAM_FACTORY.manufacturePojo(ViewTradeSignatureExpedient.class))
              .limit(10).toList();
            var page = new PageDto<>(items, 0, 10, 10);
            when(viewTradeSignatureFindAllUseCase.findAll(any(ViewTradeSignatureExpedientFindByFilterRequest.class)))
              .thenReturn(Mono.just(page));

            webClient.get()
              .uri(builder -> builder.path("/v1/trades-signatures/{originId}/view/status").build(1L))
              .exchange().expectStatus().isOk().expectBody(SignatureExpedienteStatusResponse.class)
              .consumeWith(response -> {
                  var body = Objects.requireNonNull(response.getResponseBody());
                  assertThat(items).extracting("signatureExpedientStatus")
                    .anyMatch(status -> status.equals(body.getStatus()));
              });
        }

        @Test
        void shouldReturnErrorWhenNoExpedientsFound() {
            var page = new PageDto<ViewTradeSignatureExpedient>(List.of(), 0, 10, 10);
            when(viewTradeSignatureFindAllUseCase.findAll(any(ViewTradeSignatureExpedientFindByFilterRequest.class)))
              .thenReturn(Mono.just(page));

            webClient.get()
              .uri(builder -> builder.path("/v1/trades-signatures/{originId}/view/status").build(1L))
              .exchange().expectStatus().is5xxServerError();
        }
    }

    private <T> ArgumentCaptor<T> captureArgument(Class<T> clazz) {
        return ArgumentCaptor.forClass(clazz);
    }

    @Test
    void shouldStartSignatureWorkflowSuccessfully() {
        var locale = captureArgument(Locale.class);
        var entity = captureArgument(String.class);
        var originId = captureArgument(Long.class);
        var request = captureArgument(StartSignatureRequestDto.class);

        var response = StartSignatureResponseDto.builder().expedientId(9876L).build();

        when(tradeSignatureServicePost.startSignatureWorkflow( entity.capture(), locale.capture(), originId.capture(), request.capture()))
                .thenReturn(Mono.just(response));

        var startSignatureRequest = PODAM_FACTORY.manufacturePojo(StartSignatureRequest.class);

        webClient.post()
                .uri(builder -> builder.path("/v1/trades-signatures/{originId}/signatures").build(1L))
                .bodyValue(startSignatureRequest).accept(MediaType.APPLICATION_JSON)
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049)
                .exchange().expectStatus().isOk().expectBody(StartSignatureResponse.class)
                .value(x -> assertThat(x).usingRecursiveComparison().isEqualTo(response));
    }

    private TradeSignatureResponse createMockTradeSignatureResponse() {
        return TradeSignature.builder()
                .tradeSignatureId(DEFAULT_ID)
                .build();
    }

    private WebTestClient.RequestHeadersSpec<?> preparePostRequest(String path, Object body) {
        return webClient.post()
                .uri(path)
                .bodyValue(body)
                .accept(MediaType.APPLICATION_JSON)
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049);
    }

    private void setupServiceMock(Object response) {
        when(serviceSave.createOrUpdateSignature(
                any(Locale.class), 
                any(String.class), 
                any(TradeSignatureDto.class)))
                .thenReturn(Mono.just(response));
    }

    private WebTestClient.ResponseSpec prepareGetCall(Long tradeSignatureId, Long originId, String origin,
            GetTradeSignatureDto response) {
        var locale = ArgumentCaptor.forClass(Locale.class);
        var entity = ArgumentCaptor.forClass(String.class);
        var requestDto = ArgumentCaptor.forClass(GetTradeSignatureParameterDto.class);
        var request = GetTradeSignatureRequestParameter.builder()
                .tradeSignatureId(tradeSignatureId)
                .originId(originId)
                .origin(origin)
                .build();

        when(serviceGet.getTradeSignature(locale.capture(), entity.capture(), requestDto.capture()))
                .thenReturn(Mono.just(response));

        return webClient.get()
                .uri(builder -> builder.path("/v1/trades-signatures/view")
                        //.bodyValue(getTradeSignatureRequest(ONLY_TRADE_SIGNATURE_ID)).accept(MediaType.APPLICATION_JSON)
                        .queryParam("originId", Optional.ofNullable(request.getOriginId()))
                        .queryParam("origin", Optional.ofNullable(request.getOrigin()))
                        .queryParam("tradeSignatureId", Optional.ofNullable(request.getTradeSignatureId()))
                        .build())
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049)
                .exchange();
    }

    private <T> void assertResponseEquals(T expected, T actual) {
        assertThat(actual)
            .usingRecursiveComparison()
            .isEqualTo(expected);
    }

    private PageDto<ViewTradeSignatureExpedient> createMockPage(List<ViewTradeSignatureExpedient> items) {
        return new PageDto<>(items, 0, PAGE_SIZE, items.size());
    }

}

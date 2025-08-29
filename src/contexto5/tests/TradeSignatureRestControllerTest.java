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
import com.acelera.fx.digitalsignature.application.service.UpdateSignatureExpedientStatusServiceImpl;
import com.acelera.fx.digitalsignature.application.service.ViewTradeSignatureExpedientStatusServiceImpl;
import com.acelera.fx.digitalsignature.application.usecase.port.ViewTradeSignatureFindAllUseCase;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureParameterDto;
import com.acelera.fx.digitalsignature.domain.port.dto.StartSignatureRequestDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.service.*;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.CreateExpedientResponse;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.response.SignatureExpedienteStatusResponse;
import com.acelera.locale.LocaleAutoConfig;
import com.acelera.locale.LocaleConstants;
import com.acelera.security.WebSecurityAutoConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
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
import static org.mockito.Mockito.*;

@WebFluxTest(TradeSignatureRestController.class)
@Import({ LocaleAutoConfig.class, WebSecurityAutoConfig.class, ViewTradeSignatureExpedientStatusServiceImpl.class,
    UpdateSignatureExpedientStatusServiceImpl.class, CreateTradeSignatureExpedientService.class })
@WithMockUser(username = "x1103878")
public class TradeSignatureRestControllerTest {

  private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();
  private @Autowired WebTestClient webClient;
  private @MockitoBean TradeSignatureServiceSave serviceSave;
  private @MockitoBean TradeSignatureServiceGet serviceGet;
  private @MockitoBean ViewTradeSignatureFindAllUseCase viewTradeSignatureFindAllUseCase;
  private @MockitoBean UpdateSignatureExpedientStatusService updateSignatureExpedientStatusService;
  private @MockitoBean CreateTradeSignatureExpedientService createTradeSignatureExpedientService;
  private @MockitoBean TradeSignatureServicePost tradeSignatureServicePost;

  @Autowired
  private TradeSignatureRestController tradeSignatureRestController;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void shouldReturnTradeSignatureResponseOk() throws IOException {

    var locale = ArgumentCaptor.forClass(Locale.class);
    var entity = ArgumentCaptor.forClass(String.class);
    var request = ArgumentCaptor.forClass(TradeSignatureDto.class);

    var response = TradeSignature.builder().tradeSignatureId(9876L).build();

    when(serviceSave.createOrUpdateSignature(locale.capture(), entity.capture(), request.capture()))
        .thenReturn(Mono.just(response));

    webClient.put().uri(builder -> builder.path("/v1/trades-signatures").build())
        .bodyValue(getTradeSignatureRequest(ONLY_TRADE_SIGNATURE_ID)).accept(MediaType.APPLICATION_JSON)
        .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049).exchange().expectStatus().isOk()
        .expectBody(TradeSignatureResponse.class)
        .value(x -> assertThat(x).usingRecursiveComparison().isEqualTo(response));
  }

  @Test
  void shouldReturnBadRequest() throws IOException {

    var locale = ArgumentCaptor.forClass(Locale.class);
    var entity = ArgumentCaptor.forClass(String.class);
    var request = ArgumentCaptor.forClass(TradeSignatureDto.class);

    when(serviceSave.createOrUpdateSignature(locale.capture(), entity.capture(), request.capture()))
        .thenThrow(new IllegalArgumentException("Error"));

    webClient.put().uri(builder -> builder.path("/v1/trades-signatures").build())
        .bodyValue(getTradeSignatureRequest(ONLY_TRADE_SIGNATURE_ID)).accept(MediaType.APPLICATION_JSON)
        .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049).exchange().expectStatus()
        .is5xxServerError();
  }

  @ParameterizedTest
  @CsvSource({ "9876, ,", ", 123450, 'TRADE'" })
  void shouldReturnGetResponseOk(Long tradeSignatureId, Long originId, String origin) {

    var response = GetTradeSignatureDto.builder().tradeSignatureId(9876L).build();
    var getResponse = prepareGetCall(tradeSignatureId, originId, origin, response);

    getResponse.expectStatus().isOk().expectBody(GetTradeSignatureResponse.class)
        .value(x -> assertThat(x).usingRecursiveComparison().isEqualTo(response));
  }

  @ParameterizedTest
  @CsvSource({ "9876, 123450, 'TRADE'" })
  void shouldReturnGetResponseNotOk(Long tradeSignatureId, Long originId, String origin) {

    var response = GetTradeSignatureDto.builder().tradeSignatureId(9876L).build();
    var getResponse = prepareGetCall(tradeSignatureId, originId, origin, response);

    getResponse.expectStatus().isBadRequest();
  }

  @Test
  void getSignatureExpedientStatus() {
    var items = Stream.generate(() -> PODAM_FACTORY.manufacturePojo(ViewTradeSignatureExpedient.class)).limit(10)
        .toList();
    var page = new PageDto<>(items, 0, 10, 10);
    when(viewTradeSignatureFindAllUseCase.findAll(any(ViewTradeSignatureExpedientFindByFilterRequest.class)))
        .thenReturn(Mono.just(page));

    webClient.get().uri(builder -> builder.path("/v1/trades-signatures/{originId}/view/status").build(1L)).exchange()
        .expectStatus().isOk().expectBody(SignatureExpedienteStatusResponse.class).consumeWith(response -> {
          var body = Objects.requireNonNull(response.getResponseBody());
          assertThat(items).extracting("signatureExpedientStatus").anyMatch(status -> status.equals(body.getStatus()));
        });
  }

  @Test
  void getSignatureExpedientStatusEmpty() {
    var page = new PageDto<ViewTradeSignatureExpedient>(List.of(), 0, 10, 10);
    when(viewTradeSignatureFindAllUseCase.findAll(any(ViewTradeSignatureExpedientFindByFilterRequest.class)))
        .thenReturn(Mono.just(page));

    webClient.get().uri(builder -> builder.path("/v1/trades-signatures/{originId}/view/status").build(1L)).exchange()
        .expectStatus().is5xxServerError();
  }

  private WebTestClient.ResponseSpec prepareGetCall(Long tradeSignatureId, Long originId, String origin,
      GetTradeSignatureDto response) {
    var locale = ArgumentCaptor.forClass(Locale.class);
    var entity = ArgumentCaptor.forClass(String.class);
    var requestDto = ArgumentCaptor.forClass(GetTradeSignatureParameterDto.class);
    var request = GetTradeSignatureRequestParameter.builder().tradeSignatureId(tradeSignatureId).originId(originId)
        .origin(origin).build();

    when(serviceGet.getTradeSignature(locale.capture(), entity.capture(), requestDto.capture()))
        .thenReturn(Mono.just(response));

    return webClient.get().uri(builder -> builder.path("/v1/trades-signatures/view")
        // .bodyValue(getTradeSignatureRequest(ONLY_TRADE_SIGNATURE_ID)).accept(MediaType.APPLICATION_JSON)
        .queryParam("originId", Optional.ofNullable(request.getOriginId()))
        .queryParam("origin", Optional.ofNullable(request.getOrigin()))
        .queryParam("tradeSignatureId", Optional.ofNullable(request.getTradeSignatureId())).build())
        .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049).exchange();
  }

  @Test
  void testUpdateSignatureExpedientStatus_Success() {
    String entity = "testEntity";
    Long expedientId = 123L;
    String status = "COMPLETED";

    when(updateSignatureExpedientStatusService.updateStatus(entity, expedientId, status)).thenReturn(Mono.empty());

    Mono<Void> result = tradeSignatureRestController.updateSignatureExpedientStatus(entity, expedientId, status);

    StepVerifier.create(result).verifyComplete();

    verify(updateSignatureExpedientStatusService, times(1)).updateStatus(entity, expedientId, status);
  }

  @Test
  void testUpdateSignatureExpedientStatus_Error() {
    // Arrange
    String entity = "testEntity";
    Long expedientId = 123L;
    String status = "COMPLETED";
    RuntimeException exception = new RuntimeException("Expediente no encontrado");

    when(updateSignatureExpedientStatusService.updateStatus(entity, expedientId, status))
        .thenReturn(Mono.error(exception));

    Mono<Void> result = tradeSignatureRestController.updateSignatureExpedientStatus(entity, expedientId, status);

    StepVerifier.create(result).expectErrorMatches(
        throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Expediente no encontrado"))
        .verify();

    verify(updateSignatureExpedientStatusService, times(1)).updateStatus(entity, expedientId, status);
  }


  @Test
  void testCreateSignatureExpedient_Error() {
    // Arrange
    String entity = "testEntity";
    Long originId = 123L;
    RuntimeException exception = new RuntimeException("Error en Creación de Expediente");
    CreateExpedientRequest request = null;

    when(createTradeSignatureExpedientService.createSignatureExpedient( LocaleConstants.DEFAULT_LOCALE, entity, originId, request))
            .thenReturn(Mono.error(exception));

    Mono<CreateExpedientResponse> result = tradeSignatureRestController.createSignatureExpedient(entity, LocaleConstants.DEFAULT_LOCALE, originId, request);

    StepVerifier.create(result).expectErrorMatches(
                    throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Error en Creación de Expediente"))
            .verify();

    verify(createTradeSignatureExpedientService, times(1)).createSignatureExpedient(LocaleConstants.DEFAULT_LOCALE, entity, originId, request);
  }

  @Test
  void testCreateSignatureExpedient_Success() {
    String entity = "testEntity";
    Long originId = 123L;
    CreateExpedientRequest request = null;

    CreateExpedientResponse response = CreateExpedientResponse.builder().expedientId(123L).build();

    when(createTradeSignatureExpedientService.createSignatureExpedient( LocaleConstants.DEFAULT_LOCALE, entity, originId, request))
            .thenReturn(Mono.just(response));

    Mono<CreateExpedientResponse> result = tradeSignatureRestController.createSignatureExpedient(entity, LocaleConstants.DEFAULT_LOCALE, originId, request);

    StepVerifier.create(result).expectNextMatches(expedient -> Objects.equals(expedient.getExpedientId(),
            response.getExpedientId()));

    verify(createTradeSignatureExpedientService, times(1)).createSignatureExpedient(LocaleConstants.DEFAULT_LOCALE, entity, originId, request);

  }

  @Test
  void testPostStartSignatureWorkflow_Error() {
    // Arrange
    String entity = "testEntity";
    Long originId = 123L;
    RuntimeException exception = new RuntimeException("Error en el proceso de gestión de datos de firma digital");
    StartSignatureRequestDto requestDto = null;
    StartSignatureRequest request = null;
    ServerHttpRequest httpRequest = null;

    when(tradeSignatureServicePost.startSignatureWorkflow( entity, LocaleConstants.DEFAULT_LOCALE, originId, requestDto ,httpRequest ))
            .thenReturn(Mono.error(exception));

    Mono<StartSignatureResponse> result = tradeSignatureRestController.postStartSignatureWorkflow(entity, LocaleConstants.DEFAULT_LOCALE, originId, request, httpRequest);

    StepVerifier.create(result).expectErrorMatches(
                    throwable -> throwable instanceof RuntimeException && throwable.getMessage().equals("Error en el proceso de gestión de datos de firma digital"))
            .verify();

    verify(tradeSignatureServicePost, times(1)).startSignatureWorkflow(entity, LocaleConstants.DEFAULT_LOCALE, originId, requestDto ,httpRequest);
  }

}

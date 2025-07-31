@WebFluxTest(FxTradeSignatureRestController.class)
@Import({ LocaleAutoConfig.class, WebSecurityAutoConfig.class, ErrorWebFluxAutoConfig.class,
        ErrorWebFluxAutoConfiguration.class })
@WithMockUser(username = "x1103878")
public class FxTradeSignatureRestControllerTest {

    private @Autowired WebTestClient webClient;
    private @MockitoBean FxTradeSignatureService service;
    
    // Common argument captors
    private ArgumentCaptor<Locale> localeCaptor;
    private ArgumentCaptor<String> entityCaptor;
    private ArgumentCaptor<ServerHttpRequest> requestCaptor;
    
    private static final String BASE_URL = "/v1/trades-signatures";
    private static final String ENTITY_HEADER_VALUE = LocaleConstants.ENTITY_0049;
    private static final Long SAMPLE_ID = 9876L;

    @BeforeEach
    void setUp() {
        localeCaptor = ArgumentCaptor.forClass(Locale.class);
        entityCaptor = ArgumentCaptor.forClass(String.class);
        requestCaptor = ArgumentCaptor.forClass(ServerHttpRequest.class);
    }

    @Test
    void testPutTradeSignature() {
        // Arrange
        var requestCaptor = ArgumentCaptor.forClass(TradeSignatureRequest.class);
        var response = TradeSignatureResponse.builder()
                .tradeSignatureId(SAMPLE_ID)
                .build();
        
        var request = TradeSignatureRequest.builder()
                .tradeSignatureId(SAMPLE_ID)
                .build();

        when(service.updateTradeSignature(
                entityCaptor.capture(), 
                localeCaptor.capture(), 
                requestCaptor.capture(), 
                this.requestCaptor.capture()))
                .thenReturn(Mono.just(response));

        // Act & Assert
        webClient.put()
                .uri(BASE_URL)
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .header(LocaleConstants.ENTITY_HEADER, ENTITY_HEADER_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(TradeSignatureResponse.class)
                .value(actual -> assertThat(actual)
                        .usingRecursiveComparison()
                        .isEqualTo(response));
    }

    @Test
    void testGetTradeSignature() {
        // Arrange
        var requestCaptor = ArgumentCaptor.forClass(GetTradeSignatureRequestParameter.class);
        var response = GetTradeSignatureResponse.builder()
                .tradeSignatureId(SAMPLE_ID)
                .build();
        
        var request = GetTradeSignatureRequestParameter.builder()
                .tradeSignatureId(SAMPLE_ID)
                .build();

        when(service.getTradeSignature(
                entityCaptor.capture(), 
                localeCaptor.capture(), 
                requestCaptor.capture(), 
                this.requestCaptor.capture()))
                .thenReturn(Mono.just(response));

        // Act & Assert
        webClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path(BASE_URL + "/view")
                        .queryParamIfPresent("originId", Optional.ofNullable(request.getOriginId()))
                        .queryParamIfPresent("origin", Optional.ofNullable(request.getOrigin()))
                        .queryParamIfPresent("tradeSignatureId", Optional.ofNullable(request.getTradeSignatureId()))
                        .build())
                .header(LocaleConstants.ENTITY_HEADER, ENTITY_HEADER_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(GetTradeSignatureResponse.class)
                .value(actual -> assertThat(actual)
                        .usingRecursiveComparison()
                        .isEqualTo(response));
    }

    @Test
    void testPostTradeSignature() {
        // Arrange
        var requestCaptor = ArgumentCaptor.forClass(StartSignatureRequest.class);
        var originIdCaptor = ArgumentCaptor.forClass(Long.class);
        var response = StartSignatureResponse.builder()
                .expedientId(SAMPLE_ID)
                .build();

        var request = StartSignatureRequest.builder()
                .originId(123L)
                .productId("FW")
                .build();

        when(service.postStartSignatureWorkflow(
                entityCaptor.capture(), 
                localeCaptor.capture(), 
                requestCaptor.capture(),
                originIdCaptor.capture(), 
                this.requestCaptor.capture()))
                .thenReturn(Mono.just(response));

        // Act & Assert
        webClient.post()
                .uri(BASE_URL + "/{originId}/signatures", request.getOriginId())
                .bodyValue(request)
                .accept(MediaType.APPLICATION_JSON)
                .header(LocaleConstants.ENTITY_HEADER, ENTITY_HEADER_VALUE)
                .exchange()
                .expectStatus().isOk()
                .expectBody(StartSignatureResponse.class)
                .value(actual -> assertThat(actual)
                        .usingRecursiveComparison()
                        .isEqualTo(response));
    }
}
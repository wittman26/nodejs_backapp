package contexto4.app.tests;

@ExtendWith(MockitoExtension.class)
class CreateTradeSignatureExpedientServiceImplTest {

    @Mock
    private TradeSignatureStep tradeSignatureStep;

    @Mock
    private DocumentStep documentStep;

    @Mock
    private ExpedientStep expedientStep;

    @Mock
    private TradeSignerHelper tradeSignerHelper;

    @InjectMocks
    private CreateTradeSignatureExpedientServiceImpl service;

    private final Locale locale = Locale.getDefault();
    private final String entity = "0049";
    private final Long originId = 123L;
    private final CreateExpedientRequest request = CreateExpedientRequest.builder()
            .productId("PROD1")
            .build();

    private final TradeSignature tradeSignature = new TradeSignature();
    private final List<Signer> signers = List.of(new Signer());
    private final List<ProductDocumentParameters> documentTypes = List.of(new ProductDocumentParameters());
    private final CreateExpedientResponse response = CreateExpedientResponse.builder()
            .expedientId(999L)
            .build();

    @BeforeEach
    void setUp() {
        // Configuración por defecto del origin
        Mockito.when(tradeSignerHelper.isEventProduct(Mockito.anyString())).thenReturn(false);

        // Paso 1: TradeSignature
        Mockito.when(tradeSignatureStep.obtenerTradeSignature(entity, originId, request))
                .thenReturn(Mono.just(tradeSignature));

        // Paso 2: Firmantes
        Mockito.when(tradeSignatureStep.obtenerFirmantes(locale, entity, tradeSignature, originId, "TRADE"))
                .thenReturn(Mono.just(signers));

        // Paso 3: Documentos
        Mockito.when(documentStep.obtenerTiposDeDocumento(entity, locale, request.getProductId()))
                .thenReturn(Mono.just(documentTypes));

        // Paso 4: Expediente
        Mockito.when(expedientStep.crearExpediente(documentTypes, locale, entity, originId, request, "TRADE", signers, tradeSignature))
                .thenReturn(Mono.just(response));
    }

    @Test
    void createSignatureExpedient_success() {
        StepVerifier.create(service.createSignatureExpedient(locale, entity, originId, request))
                .expectNext(response)
                .verifyComplete();

        Mockito.verify(tradeSignatureStep).obtenerTradeSignature(entity, originId, request);
        Mockito.verify(tradeSignatureStep).obtenerFirmantes(locale, entity, tradeSignature, originId, "TRADE");
        Mockito.verify(documentStep).obtenerTiposDeDocumento(entity, locale, request.getProductId());
        Mockito.verify(expedientStep).crearExpediente(documentTypes, locale, entity, originId, request, "TRADE", signers, tradeSignature);
    }

    @Test
    void createSignatureExpedient_errorEnPaso() {
        RuntimeException error = new RuntimeException("Error en paso");
        Mockito.when(tradeSignatureStep.obtenerTradeSignature(entity, originId, request))
                .thenReturn(Mono.error(error));

        StepVerifier.create(service.createSignatureExpedient(locale, entity, originId, request))
                .expectErrorMatches(e -> e instanceof RuntimeException && e.getMessage().equals("Error en paso"))
                .verify();

        Mockito.verify(tradeSignatureStep).obtenerTradeSignature(entity, originId, request);
        Mockito.verifyNoMoreInteractions(tradeSignatureStep, documentStep, expedientStep);
    }
}


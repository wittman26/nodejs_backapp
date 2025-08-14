package contexto4.app.tests;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateExpedientTradeSignatureStepImplTest {

    @Mock
    private TradeSignatureServiceGet tradeSignatureServiceGet;

    @InjectMocks
    private CreateExpedientTradeSignatureStepIml step;

    private final Locale locale = Locale.getDefault();
    private final String entity = "0049";
    private final Long originId = 123L;
    private final String origin = "TRADE";
    private final CreateExpedientRequest request = new CreateExpedientRequest("PROD1");
    private final TradeSignature tradeSignature = TradeSignature.builder()
            .tradeSignatureId(456L)
            .build();
    private final List<TradeSignerDto> signers = List.of(
            TradeSignerDto.builder()
                .signerId("SIGNER1")
                .name("Test Signer")
                .build()
    );

    @BeforeEach
    void setUp() {
        when(tradeSignatureServiceGet.getTradeSignature(entity, originId, request))
                .thenReturn(Mono.just(tradeSignature));

        GetTradeSignatureParameterDto signerRequest = GetTradeSignatureParameterDto.builder()
                .tradeSignatureId(tradeSignature.getTradeSignatureId())
                .origin(origin)
                .originId(originId)
                .build();

        when(tradeSignatureServiceGet.getTradeSignature(locale, entity, signerRequest))
                .thenReturn(Mono.just(GetTradeSignatureDto.builder()
                        .signers(signers)
                        .build()));
    }

    @Test
    void obtainTradeSignature_success() {
        StepVerifier.create(step.obtainTradeSignature(entity, originId, request))
                .expectNext(tradeSignature)
                .verifyComplete();

        verify(tradeSignatureServiceGet).getTradeSignature(entity, originId, request);
    }

    @Test
    void obtainTradeSignature_notFound() {
        when(tradeSignatureServiceGet.getTradeSignature(entity, originId, request))
                .thenReturn(Mono.empty());

        StepVerifier.create(step.obtainTradeSignature(entity, originId, request))
                .expectErrorMatches(e -> e instanceof RuntimeException 
                        && e.getMessage().equals("TradeSignature no encontrado"))
                .verify();
    }

    @Test
    void obtainSigners_success() {
        StepVerifier.create(step.obtainSigners(locale, entity, tradeSignature, originId, origin))
                .expectNext(signers)
                .verifyComplete();

        verify(tradeSignatureServiceGet).getTradeSignature(eq(locale), eq(entity), any());
    }

    @Test
    void obtainSigners_withExistingExpedient() {
        TradeSignature tradeSignatureWithExpedient = TradeSignature.builder()
                .tradeSignatureId(456L)
                .expedientId(789L)
                .build();

        StepVerifier.create(step.obtainSigners(locale, entity, tradeSignatureWithExpedient, originId, origin))
                .expectNext(signers)
                .verifyComplete();

        verify(tradeSignatureServiceGet).getTradeSignature(eq(locale), eq(entity), any());
    }

    @Test
    void obtainSigners_noSignersFound() {
        GetTradeSignatureParameterDto signerRequest = GetTradeSignatureParameterDto.builder()
                .tradeSignatureId(tradeSignature.getTradeSignatureId())
                .origin(origin)
                .originId(originId)
                .build();

        when(tradeSignatureServiceGet.getTradeSignature(locale, entity, signerRequest))
                .thenReturn(Mono.just(GetTradeSignatureDto.builder()
                        .signers(List.of())
                        .build()));

        StepVerifier.create(step.obtainSigners(locale, entity, tradeSignature, originId, origin))
                .expectNext(List.of())
                .verifyComplete();
    }
}
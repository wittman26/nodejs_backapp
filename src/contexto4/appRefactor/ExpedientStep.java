package contexto4.appRefactor;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpedientStep {

    private final CreateExpedientGetDocumentNamesUseCase getDocumentNames;
    private final CreateExpedientGetTitleAndCenterUseCase getTitleAndCenter;
    private final CreateExpedientGetClausesUseCase getClauses;
    private final CreateExpedientBuildDfdRequestUseCase buildDfdRequest;
    private final RestDfdClient restDfdClient;
    private final UpdateTradeSignatureExpedientUseCase updateTradeSignatureExpedient;

    public Mono<CreateExpedientResponse> crearExpediente(List<ProductDocumentParameters> docTypes,
                                                         Locale locale,
                                                         String entity,
                                                         Long originId,
                                                         CreateExpedientRequest request,
                                                         String origin,
                                                         List<Signer> signers,
                                                         TradeSignature tradeSignature) {

        log.info("🛠 Iniciando construcción de expediente...");

        return getDocumentNames.execute(docTypes, entity, originId, origin)
            .flatMap(documentSignatures -> getTitleAndCenter.execute(entity, originId, origin)
                .flatMap(titleAndCenter -> getClauses.execute(entity, originId, request.getProductId())
                    .flatMap(clauses -> {
                        ExpedientRequest expedientRequest = buildDfdRequest.execute(
                            titleAndCenter, clauses, documentSignatures, request, origin
                        );
                        return restDfdClient.createExpedient(expedientRequest)
                            .switchIfEmpty(Mono.error(new RuntimeException("DFD no devolvió expedienteId")))
                            .flatMap(expedientId -> updateTradeSignatureExpedient.execute(tradeSignature, expedientId)
                                .thenReturn(CreateExpedientResponse.builder().expedientId(expedientId).build())
                            );
                    })
                )
            );
    }
}

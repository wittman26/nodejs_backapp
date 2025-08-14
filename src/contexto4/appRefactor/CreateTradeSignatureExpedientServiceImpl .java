@Service
@RequiredArgsConstructor
@Slf4j
public class CreateTradeSignatureExpedientServiceImpl implements CreateTradeSignatureExpedientService {

    private final TradeSignatureStep tradeSignatureStep;
    private final DocumentStep documentStep;
    private final ExpedientStep expedientStep;
    private final TradeSignerHelper tradeSignerHelper;

    @Override
    public Mono<CreateExpedientResponse> createSignatureExpedient(Locale locale, String entity, Long originId, CreateExpedientRequest request) {
        String origin = resolveOrigin(request.getProductId());

        log.info("🚀 Iniciando creación de expediente para originId={}, producto={}, origin={}", originId, request.getProductId(), origin);

        return tradeSignatureStep.obtenerTradeSignature(entity, originId, request)
            .flatMap(ts -> tradeSignatureStep.obtenerFirmantes(locale, entity, ts, originId, origin)
                .flatMap(signers -> documentStep.obtenerTiposDeDocumento(entity, locale, request.getProductId())
                    .flatMap(docTypes -> expedientStep.crearExpediente(docTypes, locale, entity, originId, request, origin, signers, ts))
                )
            )
            .doOnSuccess(resp -> log.info("✅ Expediente creado con ID {}", resp.getExpedientId()))
            .doOnError(e -> log.error("❌ Error creando expediente: {}", e.getMessage(), e));
    }

    private String resolveOrigin(String productId) {
        return tradeSignerHelper.isEventProduct(productId) ? "EVENT" : "TRADE";
    }
}

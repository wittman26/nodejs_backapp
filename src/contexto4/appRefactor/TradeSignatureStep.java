@Service
@RequiredArgsConstructor
@Slf4j
public class TradeSignatureStep {

    private final TradeSignatureServiceGet tradeSignatureServiceGet;

    public Mono<TradeSignature> obtenerTradeSignature(String entity, Long originId, CreateExpedientRequest request) {
        log.info("📄 Buscando TradeSignature para originId={} y entity={}", originId, entity);
        return tradeSignatureServiceGet.getTradeSignature(entity, originId, request)
            .switchIfEmpty(Mono.error(new RuntimeException("TradeSignature no encontrado")));
    }

    public Mono<List<Signer>> obtenerFirmantes(Locale locale, String entity, TradeSignature tradeSignature, Long originId, String origin) {
        log.info("👥 Obteniendo firmantes para tradeSignatureId={}", tradeSignature.getTradeSignatureId());
        return tradeSignatureServiceGet.getTradeSignature(locale, entity, tradeSignature.getTradeSignatureId(), originId, origin)
            .map(GetTradeSignatureDto::getSigners)
            .doOnNext(signers -> log.info("Firmantes encontrados: {}", signers.size()));
    }
}

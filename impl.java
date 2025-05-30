@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureServiceImpl implements TradeSignatureService {

    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    private final TradeSignerRepositoryClient tradeSignerRepositoryClient;

    private static final String ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE = "Se espera incluir originId o transferId pero no ambos";

    @Override
    public Mono<CreateDocumentResponse> createDocument(String originId, Locale locale, String entity, CreateDocumentRequest request) {
        return Mono.empty();
    }

    /**
     * Crea o actualiza una firma de operación.
     * @param locale el locale
     * @param entity la entidad
     * @param request la petición de firma
     * @return Mono con la respuesta de la firma
     */
    @Override
    public Mono<TradeSignatureResponse> createOrUpdateSignature(Locale locale, String entity, TradeSignatureRequest request) {
        boolean hasTradeSignatureId = request.getTradeSignatureId() != null;
        boolean hasOriginId = request.getOriginId() != null;

        validateCreateOrUpdateParams(hasTradeSignatureId, hasOriginId);

        if (hasTradeSignatureId) {
            // Lógica de Actualización
            return updateTradeSignature(request.getTradeSignatureId(), request);
        } else {
            return saveTradeSignature(entity, request);
            // Consultar FX_TRADE_SIGNATURE, campo TRADE_SIGNATURE_ID
//            return findTradeSignatureById(entity, request.getOriginId(), request.getProductId())
//                    .flatMap(tradeSignatureResponse -> updateTradeSignature(tradeSignatureResponse.getTradeSignatureId(), request)) // Lógica de Actualización
//                    .switchIfEmpty(saveTradeSignature(request)); // Lógica de Alta
        }
    }

    /**
     * Valida los parámetros de entrada para crear o actualizar una firma.
     */
    private void validateCreateOrUpdateParams(boolean hasTradeSignatureId, boolean hasOriginId) {
        if (hasTradeSignatureId == hasOriginId) {
            log.error(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
            throw new IllegalArgumentException(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
        }
    }

    private Mono<TradeSignatureResponse> saveTradeSignature(String entity, TradeSignatureRequest request) {
        TradeSignature tradeSignature = TradeSignatureMapper.INSTANCE.toTradeSignature(request);
        tradeSignature.setEntity(entity);
        tradeSignature.setOrigin(
                isEventProduct(tradeSignature.getProductId()) ? "EVENT" : "TRADE");

        tradeSignature.setTradeSignerList(
                request.getSigners()
                        .stream().map(TradeSignerMapper.INSTANCE::toTradeSigner).toList()
        );

        return tradeSignatureRepositoryClient.save(tradeSignature)
                .flatMap(response -> createResponse(tradeSignature.getTradeSignatureId()));
    }

    private boolean isEventProduct(String productId) {
        return Arrays.asList("AN", "IN", "PC", "PS").contains(productId);
    }

    private Mono<TradeSignatureResponse> findTradeSignatureById(String entity, Integer originId, String productId) {
        return tradeSignatureRepositoryClient.findByParams(entity, originId, productId)
                .flatMap(tradeSignature -> createResponse(tradeSignature.getTradeSignatureId()))
                .switchIfEmpty(Mono.empty());
    }

    private Mono<TradeSignatureResponse> updateTradeSignature(Integer tradeSignatureId, TradeSignatureRequest request) {

        List<TradeSignerRequest> incoming = request.getSigners();

        // 1. Obtener TradeSigners por TradeSignatureId
        return tradeSignerRepositoryClient.findTradeSignersByTradeSignatureId(tradeSignatureId)
                .flatMap(existing -> sycronizeTradeSigners(tradeSignatureId, incoming, existing)
                .then(tradeSignatureRepositoryClient.update(
                        tradeSignatureId, TradeSignatureMapper.INSTANCE.toTradeSignature(request)))
                .flatMap(tradeSignature -> createResponse(tradeSignature.getTradeSignatureId()))
        );
    }

    private Mono<Void> sycronizeTradeSigners(Integer tradeSignatureId, List<TradeSignerRequest> incoming, List<TradeSigner> existing) {
        Set<String> existingIds = existing.stream().map(TradeSigner::getSignerId).filter(Objects::nonNull).collect(Collectors.toSet());
        Set<String> incomingIds = incoming.stream().map(TradeSignerRequest::getSignerId).filter(Objects::nonNull).collect(Collectors.toSet());

        return Flux.concat(
                deleteRemovedTradeSigners(existing, incomingIds),
                insertNewTradeSigners(tradeSignatureId, incoming),
                updateExistingTradeSigners(tradeSignatureId, incoming, existingIds)
        ).then();
    }

    private Flux<Void> deleteRemovedTradeSigners(List<TradeSigner> existing, Set<String> incomingIds) {
        return Flux.fromIterable(
                existing.stream()
                        .filter(tradeSigner -> !incomingIds.contains(tradeSigner.getSignerId()))
                        .map(TradeSigner::getTradeSignerId)
                        .map(tradeSignerRepositoryClient::delete)
                        .collect(Collectors.toList())
        ).flatMap(mono -> mono);
    }

    private Flux<Void> insertNewTradeSigners(Integer tradeSignatureId, List<TradeSignerRequest> incoming) {
        return Flux.fromIterable(
                incoming.stream()
                        .filter(tradeSignerRequest -> tradeSignerRequest.getSignerId() == null)
                        .map(tradeSignerRequest -> {
                            TradeSigner tradeSignerToSave = TradeSignerMapper.INSTANCE.toTradeSigner(tradeSignerRequest);
                            tradeSignerToSave.setTradeSignatureId(tradeSignatureId);
                            return tradeSignerRepositoryClient.save(tradeSignerToSave);
                        })
                        .toList()
        ).flatMap(Mono::then);
    }

    private Publisher<Void> updateExistingTradeSigners(Integer tradeSignatureId, List<TradeSignerRequest> incoming, Set<String> existingIds) {
        return Flux.fromIterable(
                incoming.stream()
                        .filter(tradeSignerRequest -> tradeSignerRequest.getSignerId() != null && existingIds.contains(tradeSignerRequest.getSignerId()))
                        .map(tradeSignerRequest -> {
                            var tradeSignerToSave = TradeSignerMapper.INSTANCE.toTradeSigner(tradeSignerRequest);
                            tradeSignerToSave.setTradeSignatureId(tradeSignatureId);
                            return tradeSignerRepositoryClient.save(tradeSignerToSave);
                        })
                        .collect(Collectors.toList())
        ).flatMap(Mono::then);
    }

    private Mono<TradeSignatureResponse> createResponse(Long tradeSignatureId) {
        return Mono.just(TradeSignatureResponse.builder().tradeSignatureId(
                tradeSignatureId.intValue()).build());
    }

}

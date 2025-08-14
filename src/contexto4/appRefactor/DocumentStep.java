@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentStep {

    private final ProductDocumentsService productDocumentsService;

    public Mono<List<ProductDocumentParameters>> obtenerTiposDeDocumento(String entity, Locale locale, String productId) {
        log.info("📄 Obteniendo tipos de documentos para producto={}", productId);
        return productDocumentsService.findProductDocumentType(entity, locale, productId)
            .collectList()
            .switchIfEmpty(Mono.error(new RuntimeException("No se encontraron tipos de documento para el producto: " + productId)))
            .doOnNext(docs -> log.info("Tipos de documento encontrados: {}", docs.size()));
    }
}

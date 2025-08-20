@ExtendWith(MockitoExtension.class)
class CreateExpedientBuildDfdRequestUseCaseImplTest {

    @InjectMocks
    private CreateExpedientBuildDfdRequestUseCaseImpl useCase;

    private static final String ORIGIN_TRADE = "TRADE";

    // --- Helpers/Test Builders ---

    private DocumentSignature buildDocumentSignature(Long id) {
        return DocumentSignature.builder()
                .id(id)
                .type("DOCUMENT")
                .indPreContractual(true)
                .build();
    }

    private CreateExpedientRequest buildRequest(String productId) {
        return CreateExpedientRequest.builder()
                .productId(productId)
                .build();
    }

    private Tuple3<String, String, String> buildTitleAndCenter() {
        return Tuples.of("Owner Name", "Owner Document", "0001");
    }

    // --- Tests ---

    @Test
    void shouldBuildExpedientRequestWithMandatoryFields_whenValidDataProvided() {
        // given
        List<DocumentSignature> docs = List.of(buildDocumentSignature(1L));
        Tuple3<String, String, String> titleAndCenter = buildTitleAndCenter();
        String disclaimer = "Some disclaimer";
        CreateExpedientRequest request = buildRequest("PROD1");

        // when
        ExpedientRequest expedientRequest = useCase.execute(titleAndCenter, disclaimer, docs, request, ORIGIN_TRADE);

        // then
        assertThat(expedientRequest).isNotNull();
        assertThat(expedientRequest.getCentre()).isEqualTo("0001");
        assertThat(expedientRequest.getCustomerId()).isEqualTo("Owner Document");
        assertThat(expedientRequest.getDocs()).hasSize(1);
        assertThat(expedientRequest.getDocs().get(0).getTypeDoc()).isEqualTo("DOCUMENT");
        assertThat(expedientRequest.getDocs().get(0).isIndPreContractual()).isTrue();
    }

    @Test
    void shouldIncludeDisclaimerInExpedientRequest_whenProvided() {
        // given
        List<DocumentSignature> docs = List.of(buildDocumentSignature(2L));
        Tuple3<String, String, String> titleAndCenter = buildTitleAndCenter();
        String disclaimer = "Important disclaimer";
        CreateExpedientRequest request = buildRequest("PROD2");

        // when
        ExpedientRequest expedientRequest = useCase.execute(titleAndCenter, disclaimer, docs, request, ORIGIN_TRADE);

        // then
        assertThat(expedientRequest.getClauses()).isEqualTo("Important disclaimer");
    }

    @Test
    void shouldThrowException_whenNoDocumentsProvided() {
        // given
        List<DocumentSignature> emptyDocs = List.of();
        Tuple3<String, String, String> titleAndCenter = buildTitleAndCenter();
        CreateExpedientRequest request = buildRequest("PROD3");

        // when / then
        assertThatThrownBy(() -> useCase.execute(titleAndCenter, "Some disclaimer", emptyDocs, request, ORIGIN_TRADE))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("documentos");
    }
}

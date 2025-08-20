package com.acelera.fx.digitalsignature.application.usecase;

import com.acelera.broker.entidades.basicas.component.VariableClient;
import com.acelera.broker.fx.db.domain.dto.DocumentSignature;
import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.application.usecase.impl.CreateExpedientBuildDfdRequestUseCaseImpl;
import com.acelera.fx.digitalsignature.domain.port.dto.SignerDocumentDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.infrastructure.adapter.rest.request.CreateExpedientRequest;
import com.acelera.locale.MessageSourceHolder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.context.support.StaticMessageSource;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

import java.util.List;
import java.util.Locale;

import static com.acelera.fx.digitalsignature.infrastructure.util.TradeSignatureConstants.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateExpedientBuildDfdRequestUseCaseImplTest {

    @Mock
    private VariableClient variableClient;

    @InjectMocks
    private CreateExpedientBuildDfdRequestUseCaseImpl useCase;

    private static final Long ORIGIN_ID = 123L;
    private static final String PRODUCT_ID = "PROD1";
    private static final String OWNER_NAME = "Owner Name";
    private static final String OWNER_DOCUMENT = "A28269983";
    private static final String CENTER = "5494";
    private static final String OWNER_JURIDIC = "J000093186";
    private static final String OWNER_NON_JURIDIC = "P000012345";
    private static final String OWNER_JURIDIC_NO_ZEROS = "J93186";

    private static final String VALIDITY_DAYS_VAIABLE = "FX_SIGNATURE_VALIDITY_DAYS";
    private static final String OPER_CODE_BUILT = OPER_CODE_TRADE + ORIGIN_ID;
    private static final String SUORCE_APP_CODE = "ACELERA";
    private static final String KD_DOCUMENT_TYPE = "KD";
    private static final String DOCUMENTAL_TYPE_DOC = "COMVEN_FX_PREC";
    private static final String DOCUMENTAL_CODE_DOC = "di";

    private final CreateExpedientRequest request;
    private final Tuple4<String, String, String, String> titleAndCenterData;
    private final List<DocumentSignature> documentSignatures;
    private final List<ProductDocumentParameters> documentTypes;
    private final List<TradeSignerDto> signers;
    private final List<ExpedientRequest.Clause> clauses;

    CreateExpedientBuildDfdRequestUseCaseImplTest() {
        request = new CreateExpedientRequest(PRODUCT_ID);

        titleAndCenterData = Tuples.of(OWNER_NAME, OWNER_DOCUMENT, CENTER, OWNER_JURIDIC);

        documentSignatures = List.of(DocumentSignature.builder()
                .idTipDoc(KD_DOCUMENT_TYPE)
                .nombreDocumento("document1.pdf")
                .build());

        var prod = new ProductDocumentParameters();
        prod.setDocumentType(KD_DOCUMENT_TYPE);
        prod.setDocumentalTypeDoc(DOCUMENTAL_TYPE_DOC);
        prod.setDocumentalCodeDoc(DOCUMENTAL_CODE_DOC);
        prod.setIsPrecontractual("Y");

        documentTypes = List.of(prod);

        signers = List.of(TradeSignerDto.builder()
                .signerId("S0001")
                .name("Signer Name")
                .document(SignerDocumentDto.builder()
                        .type("N")
                        .number(null)
                        .build())
                .interventionType("01")
                .build());

        clauses = List.of(ExpedientRequest.Clause.builder()
                .idClause("CL1")
                .clauseContent("Clause content")
                .build());
    }

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(useCase, "sourceAppUrlBasePath", "http://localhost");
        ReflectionTestUtils.setField(useCase, "s3Bucket", "test-bucket");
        ReflectionTestUtils.setField(useCase, "s3folder", "test-folder");

        when(variableClient.find(VALIDITY_DAYS_VAIABLE)).thenReturn(Mono.just("5"));

        LocaleContextHolder.setLocale(Locale.US, true);
        var ms = new StaticMessageSource();
        ms.addMessage("FX_SIGNATURE_VALIDITY_DAYS no encontrado: ", Locale.US, "Failed to deserialize JSON object");
        MessageSourceHolder.setMessageSource(ms);
    }

    @Test
    void buildDfdRequest_success() {
        StepVerifier.create(useCase.buildDfdRequest(titleAndCenterData, clauses, documentSignatures,
                        request, ORIGIN_TRADE, documentTypes, signers, ORIGIN_ID))
                .expectNextMatches(expedientRequest ->
                        validateExpedientRequest(expedientRequest) &&
                                validateDocuments(expedientRequest.getDocs()))
                .verifyComplete();

        verify(variableClient).find(VALIDITY_DAYS_VAIABLE);
    }

    @Test
    void buildDfdRequest_validityDaysNotFound() {
        when(variableClient.find(VALIDITY_DAYS_VAIABLE))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.buildDfdRequest(titleAndCenterData, clauses, documentSignatures,
                        request, ORIGIN_TRADE, documentTypes, signers, ORIGIN_ID))
                .expectErrorMatches(e -> e instanceof CustomErrorException)
                .verify();
    }

    @Test
    void buildDfdRequest_nonJuridicPerson_success() {
        // Change center to non-juridic
        Tuple4<String, String, String, String> nonJuridicData =
                Tuples.of(OWNER_NAME, OWNER_DOCUMENT, CENTER, OWNER_NON_JURIDIC);

        StepVerifier.create(useCase.buildDfdRequest(nonJuridicData, clauses, documentSignatures,
                        request, ORIGIN_TRADE, documentTypes, signers, ORIGIN_ID))
                .expectNextMatches(expedientRequest ->
                        !expedientRequest.isIndicatorBusinnessMailBox() &&
                                expedientRequest.isIndicatorParticularMailBox() &&
                                validateSignersWithoutRepresented(expedientRequest.getDocs().getFirst().getSigners()))
                .verifyComplete();
    }

    @Test
    void buildDfdRequest_documentTypeNotFound() {
        // Create document signature with different document type
        List<DocumentSignature> wrongSignatures = List.of(DocumentSignature.builder()
                .idTipDoc("WRONG_TYPE")
                .nombreDocumento("document1.pdf")
                .build());

        StepVerifier.create(useCase.buildDfdRequest(titleAndCenterData, clauses, wrongSignatures,
                        request, ORIGIN_TRADE, documentTypes, signers, ORIGIN_ID))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Document signature not found for type " + KD_DOCUMENT_TYPE))
                .verify();
    }

    private boolean validateExpedientRequest(ExpedientRequest request) {
        return request.getSourceApp() != null &&
                request.getSourceApp().getOperCode().equals(OPER_CODE_BUILT) &&
                request.getSourceApp().getCode().equals(SUORCE_APP_CODE) &&
                request.getSourceApp().getUrl().equals(ReflectionTestUtils.getField(useCase,"sourceAppUrlBasePath")  + SOURCE_APP_URL) &&
                request.getStartDate() != null &&
                request.getEndDate() != null &&
                request.getCentre().equals(CENTER) &&
                request.getCustomerId().equals(OWNER_JURIDIC) &&
                !request.isIndicatorBusinnessMailBox() &&
                request.isIndicatorParticularMailBox() &&
                request.getClauses().equals(clauses);
    }

    private boolean validateDocuments(List<ExpedientRequest.Document> documents) {
        if (documents.size() != 1) return false;

        ExpedientRequest.Document doc = documents.getFirst();
        return doc.getTypeDoc().equals(DOCUMENTAL_TYPE_DOC) &&
                doc.getDocumentCode().equals(DOCUMENTAL_CODE_DOC) &&
                doc.isIndPreContractual() &&
                doc.getPersonDocNumber().equals(OWNER_DOCUMENT) &&
                validateS3(doc.getS3()) &&
                validateSigners(doc.getSigners()) &&
                validateMetadata(doc.getMetadata());
    }

    private boolean validateS3(ExpedientRequest.Document.S3 s3) {
        return s3.getBucket().equals("test-bucket") &&
                s3.getFolder().equals("test-folder") &&
                s3.getKey().equals("document1.pdf");
    }

    private boolean validateSigners(List<ExpedientRequest.Document.Signer> signers) {
        if (signers.size() != 1) return false;

        ExpedientRequest.Document.Signer signer = signers.getFirst();
        return signer.getSigningPerson().equals("S1") &&
                signer.getSigningName().equals("Signer Name") &&
                signer.getInterventionType().equals("01") &&
                signer.getLocationSign().isEmpty() &&
                signer.getOrder() == 1 &&
                validateRepresented(signer.getRepresented());
    }

    private boolean validateSignersWithoutRepresented(List<ExpedientRequest.Document.Signer> signers) {
        if (signers.size() != 1) return false;

        ExpedientRequest.Document.Signer signer = signers.getFirst();
        return signer.getSigningPerson().equals("S1") &&
                signer.getSigningName().equals("Signer Name") &&
                signer.getInterventionType().equals("01") &&
                signer.getLocationSign().isEmpty() &&
                signer.getOrder() == 1 &&
                signer.getRepresented().isEmpty(); // Represented list should be empty for non-juridic
    }

    private boolean validateRepresented(List<ExpedientRequest.Document.Signer.Represented> represented) {
        if (represented.size() != 1) return false;

        ExpedientRequest.Document.Signer.Represented rep = represented.getFirst();
        return rep.getRepresentedCode().equals(OWNER_JURIDIC_NO_ZEROS) &&
                rep.getRepresentedName().equals(OWNER_NAME);
    }

    private boolean validateMetadata(ExpedientRequest.Document.Metadata metadata) {
        return metadata.getGnDate() != null &&
                metadata.getGnCreationDate() != null &&
                metadata.isGnDocOrig() &&
                metadata.getProducto().equals(PRODUCT_ID);
    }
}

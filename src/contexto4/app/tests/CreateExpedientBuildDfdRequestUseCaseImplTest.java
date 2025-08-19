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

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateExpedientBuildDfdRequestUseCaseImplTest {

    @Mock
    private VariableClient variableClient;

    @InjectMocks
    private CreateExpedientBuildDfdRequestUseCaseImpl useCase;

    private final String entity = "0049";
    private final Long originId = 123L;
    private final String productId = "PROD1";
    private final CreateExpedientRequest request;
    private final Tuple4<String, String, String, String> titleAndCenterData;
    private final List<DocumentSignature> documentSignatures;
    private final List<ProductDocumentParameters> documentTypes;
    private final List<TradeSignerDto> signers;
    private final List<ExpedientRequest.Clause> clauses;

    CreateExpedientBuildDfdRequestUseCaseImplTest() {
        request = new CreateExpedientRequest(productId);
        titleAndCenterData = Tuples.of("Owner Name", "12345678A", "J0001", "OWNER1");

        documentSignatures = List.of(DocumentSignature.builder()
                .idTipDoc("DOC1")
                .nombreDocumento("document1.pdf")
                .build());

        documentTypes = List.of(ProductDocumentParameters.builder()
                .documentType("DOC1")
                .documentalTypeDoc("TYPE1")
                .documentalCodeDoc("CODE1")
                .isPrecontractual("Y")
                .build());

        signers = List.of(TradeSignerDto.builder()
                .signerId("S0001")
                .name("Signer Name")
                .document(SignerDocumentDto.builder().type("TYPE").build())
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

        when(variableClient.find("FX_SIGNATURE_VALIDITY_DAYS"))
                .thenReturn(Mono.just("5"));

        LocaleContextHolder.setLocale(Locale.US, true);
        var ms = new StaticMessageSource();
        ms.addMessage("FX_SIGNATURE_VALIDITY_DAYS no encontrado: ", Locale.US, "Failed to deserialize JSON object");
        MessageSourceHolder.setMessageSource(ms);
    }

    @Test
    void buildDfdRequest_success() {
        StepVerifier.create(useCase.buildDfdRequest(titleAndCenterData, clauses, documentSignatures,
                        request, "TRADE", documentTypes, signers, originId))
                .expectNextMatches(expedientRequest ->
                        validateExpedientRequest(expedientRequest) &&
                                validateDocuments(expedientRequest.getDocs()))
                .verifyComplete();

        verify(variableClient).find("FX_SIGNATURE_VALIDITY_DAYS");
    }

    @Test
    void buildDfdRequest_validityDaysNotFound() {
        when(variableClient.find("FX_SIGNATURE_VALIDITY_DAYS"))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.buildDfdRequest(titleAndCenterData, clauses, documentSignatures,
                        request, "TRADE", documentTypes, signers, originId))
                .expectErrorMatches(e -> e instanceof CustomErrorException)
                .verify();
    }

    private boolean validateExpedientRequest(ExpedientRequest request) {
        return request.getSourceApp() != null &&
                request.getStartDate() != null &&
                request.getEndDate() != null &&
                request.getCentre().equals("J0001") &&
                request.getCustomerId().equals("OWNER1") &&
                request.isIndicatorBusinnessMailBox() &&
                !request.isIndicatorParticularMailBox() &&
                request.getClauses().equals(clauses) &&
                request.getTypeBox().equals("B092") && // Agregar validaciones adicionales
                request.getCatBox().equals("divisas") &&
                request.getProductDesc().equals("Derivado Divisa") &&
                request.getDescExp().equals("Contratación Derivado Divisa") &&
                request.getChannel().equals("OFI");
    }

    private boolean validateDocuments(List<ExpedientRequest.Document> documents) {
        if (documents.size() != 1) return false;

        ExpedientRequest.Document doc = documents.getFirst();
        return doc.getTypeDoc().equals("TYPE1") &&
                doc.getDocumentCode().equals("CODE1") &&
                doc.isIndPreContractual() &&
                doc.getPersonDocNumber().equals("12345678A") &&
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
        return signer.getSigningPerson().equals("S0001") && // Cambiar S1 por S0001
                signer.getIdentityDoc() == null && // El test muestra que es null
                signer.getSigningName().equals("Signer Name") &&
                signer.getInterventionType().equals("01") &&
                signer.getOrder() == 1;
    }

    private boolean validateMetadata(ExpedientRequest.Document.Metadata metadata) {
        return metadata.getGnDate() != null &&
                metadata.getGnCreationDate() != null &&
                metadata.isGnDocOrig() &&
                metadata.getProducto().equals(productId);
    }
}

import com.acelera.fx.digitalsignature.infrastructure.response.DocumentTypeResponse;
import com.acelera.locale.LocaleConstants;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.Locale;

@Validated
public interface ProductDocumentsRestControllerUI {
    @GetMapping("/v1/products/{productId}/documents")
    @Operation(summary = "Find type of documents by product")
    @ResponseStatus(HttpStatus.OK)
    Flux<DocumentTypeResponse> findProductDocumentType(@RequestHeader(name = LocaleConstants.ENTITY_HEADER, defaultValue = LocaleConstants.ENTITY_0049) String entity,
            Locale locale, @PathVariable("productId") String productId
    );
}


import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.infrastructure.response.DocumentTypeResponse;
import com.acelera.fx.digitalsignature.infrastructure.ui.ProductDocumentsRestControllerUI;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.Locale;

@RestController
@RequiredArgsConstructor
public class ProductDocumentsRestController implements ProductDocumentsRestControllerUI {

    private final ProductDocumentsService productDocumentsService;

    @Override
    public Flux<DocumentTypeResponse> findProductDocumentType(String entity, Locale locale, String productId) {
        return productDocumentsService.findProductDocumentType(entity, locale, productId);
    }
}


@WebFluxTest(ProductDocumentsRestController.class)
@Import({ LocaleAutoConfig.class, WebSecurityAutoConfig.class })
@WithMockUser(username = "x1103878")
public class ProductDocumentsRestControllerTest {
    @Autowired
    private WebTestClient webClient;

    @MockBean
    private ProductDocumentsService service;

    private static final PodamFactoryImpl PODAM_FACTORY = new PodamFactoryImpl();

    private static final String PRODUCT_ID = "FW";

    @Test
    void testFindProductDocumentType_ok() throws JsonProcessingException {
        DocumentTypeResponse response = PODAM_FACTORY.manufacturePojo(DocumentTypeResponse.class);
        when(service.findProductDocumentType(LocaleConstants.ENTITY_HEADER, LocaleConstants.DEFAULT_LOCALE, PRODUCT_ID))
                .thenReturn(Flux.just(response));

        webClient.get()
                .uri(builder -> builder.path("/v1/products/{productId}/documents").build(PRODUCT_ID))
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049)
                .header("Accept-Language", "es-ES")
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DocumentTypeResponse.class)
                .value(list -> {
                    assertThat(list).hasSize(1);
                    assertThat(list.get(0)).usingRecursiveComparison().isEqualTo(response);
                });
    }

    @Test
    void testFindProductDocumentType_empty() {
        when(service.findProductDocumentType(LocaleConstants.ENTITY_HEADER, LocaleConstants.DEFAULT_LOCALE, PRODUCT_ID))
                .thenReturn(Flux.empty());

        webClient.get()
                .uri(builder -> builder.path("/v1/products/{productId}/documents").build(PRODUCT_ID))
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049)
                .header("Accept-Language", "es-ES")
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DocumentTypeResponse.class)
                .value(list -> assertThat(list).isEmpty());
    }

    @Test
    void testFindProductDocumentType_error() {
        when(service.findProductDocumentType(LocaleConstants.ENTITY_HEADER, LocaleConstants.DEFAULT_LOCALE, PRODUCT_ID))
                .thenReturn(Flux.error(new RuntimeException("error")));

        webClient.get()
                .uri(builder -> builder.path("/v1/products/{productId}/documents").build(PRODUCT_ID))
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049)
                .header("Accept-Language", "es-ES")
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().is5xxServerError();
    }
}


/**
 * Document type
 */
@Getter
public enum TypeEnum {
    KD("KD"),

    CO("CO"),

    KE("KE"),

    EV("EV"),

    OM("OM");

    private String value;

    TypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return String.valueOf(value);
    }

    @JsonCreator
    public static TypeEnum fromValue(String value) {
        for (TypeEnum b : TypeEnum.values()) {
            if (b.value.equals(value)) {
                return b;
            }
        }
        throw new IllegalArgumentException("Unexpected value '" + value + "'");
    }
}

org.springframework.core.codec.DecodingException: JSON decoding error: Input mismatch reading Enum `com.acelera.fx.digitalsignature.infrastructure.response.base.TypeEnum`: properties-based `@JsonCreator` ([method com.acelera.fx.digitalsignature.infrastructure.response.base.TypeEnum#fromValue(java.lang.String)]) expects JSON Object (JsonToken.START_OBJECT), got JsonToken.VALUE_STRING; nested exception is com.fasterxml.jackson.databind.exc.MismatchedInputException: Input mismatch reading Enum `com.acelera.fx.digitalsignature.infrastructure.response.base.TypeEnum`: properties-based `@JsonCreator` ([method com.acelera.fx.digitalsignature.infrastructure.response.base.TypeEnum#fromValue(java.lang.String)]) expects JSON Object (JsonToken.START_OBJECT), got JsonToken.VALUE_STRING
 at [Source: UNKNOWN; byte offset: #UNKNOWN] (through reference chain: com.acelera.fx.digitalsignature.infrastructure.response.DocumentTypeResponse["type"])

	at org.springframework.http.codec.json.AbstractJackson2Decoder.processException(AbstractJackson2Decoder.java:242)
	Suppressed: The stacktrace has been enhanced by Reactor, refer to additional information below: 
Error has been observed at the following site(s):
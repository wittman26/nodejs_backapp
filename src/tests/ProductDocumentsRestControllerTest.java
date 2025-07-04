package com.acelera.fx.digitalsignature.infrastructure.controller;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.error.ErrorWebFluxAutoConfig;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.infrastructure.mapper.ProductDocumentsMapper;
import com.acelera.fx.digitalsignature.infrastructure.response.DocumentTypeResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.base.TypeEnum;
import com.acelera.locale.LocaleAutoConfig;
import com.acelera.locale.LocaleConstants;
import com.acelera.security.WebSecurityAutoConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.error.ErrorWebFluxAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.when;

@WebFluxTest(ProductDocumentsRestController.class)
@Import({ LocaleAutoConfig.class, WebSecurityAutoConfig.class, ErrorWebFluxAutoConfig.class,
        ErrorWebFluxAutoConfiguration.class })
@WithMockUser(username = "x1103878")
public class ProductDocumentsRestControllerTest {
    @Autowired
    private WebTestClient webClient;

    @MockitoBean
    private ProductDocumentsService service;

    private static final PodamFactoryImpl PODAM_FACTORY = new PodamFactoryImpl();

    private static final String PRODUCT_ID = "FW";

    @Test
    void testFindProductDocumentType_ok() {
        ProductDocumentParameters response = PODAM_FACTORY.manufacturePojo(ProductDocumentParameters.class);
        response.setDocumentType(String.valueOf(TypeEnum.KD));

        when(service.findProductDocumentType(LocaleConstants.ENTITY_0049, LocaleConstants.DEFAULT_LOCALE, PRODUCT_ID))
                .thenReturn(Flux.just(response));

        DocumentTypeResponse expected = ProductDocumentsMapper.INSTANCE.toDocumentTypeResponse(response);

        webClient.get()
                .uri(builder -> builder.path("/v1/products/{productId}/documents").build(PRODUCT_ID))
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049)
                .header("Accept-Language", LocaleConstants.DEFAULT_LANGUAGE)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DocumentTypeResponse.class)
                .value(list -> {
                    assertThat(list).asList().hasSize(1);
                    assertThat(list.getFirst()).usingRecursiveComparison().isEqualTo(expected);
                });
    }

    @Test
    void testFindProductDocumentType_empty() {
        when(service.findProductDocumentType(LocaleConstants.ENTITY_HEADER, LocaleConstants.DEFAULT_LOCALE, PRODUCT_ID))
                .thenReturn(Flux.empty());

        webClient.get()
                .uri(builder -> builder.path("/v1/products/{productId}/documents").build(PRODUCT_ID))
                .header(LocaleConstants.ENTITY_HEADER, LocaleConstants.ENTITY_0049)
                .header("Accept-Language", LocaleConstants.DEFAULT_LANGUAGE)
                .acceptCharset(StandardCharsets.UTF_8)
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(DocumentTypeResponse.class)
                .value(list -> assertThat(list).asList().isEmpty());
    }
}

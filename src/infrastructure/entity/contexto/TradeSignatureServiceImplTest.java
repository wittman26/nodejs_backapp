package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.broker.fx.db.domain.dto.TradeSigner;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.fx.db.domain.port.TradeSignatureViewRepositoryClient;
import com.acelera.fx.digitalsignature.domain.TradeSignatureDomainService;
import com.acelera.fx.digitalsignature.infrastructure.request.SignerDocument;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignerRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignatureResponse;
import com.acelera.locale.LocaleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.io.IOException;
import java.util.List;

import static com.acelera.fx.digitalsignature.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
public class TradeSignatureServiceImplTest {

    @InjectMocks
    private TradeSignatureServiceImpl tradeSignatureServiceImpl;

    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    @Mock
    private TradeSignatureDomainService tradeSignatureDomainService;

    @Mock
    private TradeSignatureViewRepositoryClient tradeSignatureViewRepositoryClient;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();


    @BeforeEach
    void setUp() {
        openMocks(this);
        tradeSignatureServiceImpl = new TradeSignatureServiceImpl(tradeSignatureDomainService);
    }

    /**
     * Validates that originId or tradeSignatureId is sent on Request but not both
     */
    @Test
    void createOrUpdateSignature_shouldThrowException_whenBothIdsPresentOrAbsent() {
        assertThrows(
                IllegalArgumentException.class,
                () -> tradeSignatureServiceImpl.createOrUpdateSignature(
                        LocaleConstants.DEFAULT_LOCALE,
                        LocaleConstants.ENTITY_0049,
                        getTradeSignatureRequest(INVALID_REQUEST))
        );
    }

    @Test
    void upsertTradeSignature_shouldThrowException_whenOriginIdIsNullOnCreate() throws IOException {
        TradeSignatureRequest request = getTradeSignatureRequest(ONLY_TRADE_SIGNATURE_ID);
        // Simula que el request no tiene originId y es un alta (no se encuentra en base)
        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.empty());

        // Forzar originId a null
        request.setOriginId(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> tradeSignatureServiceImpl.createOrUpdateSignature(
                        LocaleConstants.DEFAULT_LOCALE,
                        LocaleConstants.ENTITY_0049,
                        request).block()
        );
    }

    @Test
    void createOrUpdateSignature_shouldCreateNewTradeSignature_whenNotFound() throws IOException {
        TradeSignatureRequest request = getTradeSignatureRequest(ONLY_ORIGIN_ID);
        TradeSignature savedSignature = TradeSignature.builder().tradeSignatureId(123L).originId(request.getOriginId()).build();

        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.empty());
        when(tradeSignatureRepositoryClient.save(any(TradeSignature.class))).thenReturn(Mono.just(savedSignature));

        TradeSignatureResponse response = tradeSignatureServiceImpl
                .createOrUpdateSignature(LocaleConstants.DEFAULT_LOCALE, LocaleConstants.ENTITY_0049, request)
                .block();

        assertNotNull(response);
        assertEquals(123, response.getTradeSignatureId());
        verify(tradeSignatureRepositoryClient).save(any(TradeSignature.class));
    }

    @Test
    void createOrUpdateSignature_shouldUpdateTradeSignature_whenFound() throws IOException {
        TradeSignatureRequest request = getTradeSignatureRequest(ONLY_TRADE_SIGNATURE_ID);
        TradeSignature foundSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        TradeSignature updatedSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        updatedSignature.setTradeSignatureId(request.getTradeSignatureId());

        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.just(foundSignature));
        when(tradeSignatureRepositoryClient.save(any(TradeSignature.class))).thenReturn(Mono.just(updatedSignature));

        TradeSignatureResponse response = tradeSignatureServiceImpl
                .createOrUpdateSignature(LocaleConstants.DEFAULT_LOCALE, LocaleConstants.ENTITY_0049, request)
                .block();

        assertNotNull(response);
        assertEquals(request.getTradeSignatureId().intValue(), response.getTradeSignatureId());
        verify(tradeSignatureRepositoryClient).save(any(TradeSignature.class));
    }

    @Test
    void upsertTradeSignature_shouldSetPendingAndEventProperly() throws IOException {
        TradeSignatureRequest request = getTradeSignatureRequest(ONLY_ORIGIN_ID);
        request.setProductId("AN"); // Producto de tipo evento

        TradeSignature savedSignature = TradeSignature.builder()
                .tradeSignatureId(456L)
                .originId(request.getOriginId())
                .validatedBo("PENDING")
                .origin("EVENT")
                .build();

        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.empty());
        when(tradeSignatureRepositoryClient.save(any(TradeSignature.class))).thenReturn(Mono.just(savedSignature));

        TradeSignatureResponse response = tradeSignatureServiceImpl
                .createOrUpdateSignature(LocaleConstants.DEFAULT_LOCALE, LocaleConstants.ENTITY_0049, request)
                .block();

        assertNotNull(response);
        assertEquals(456, response.getTradeSignatureId());
        ArgumentCaptor<TradeSignature> captor = ArgumentCaptor.forClass(TradeSignature.class);
        verify(tradeSignatureRepositoryClient).save(captor.capture());
        TradeSignature toSave = captor.getValue();
        assertEquals("PENDING", toSave.getValidatedBo());
        assertEquals("EVENT", toSave.getOrigin());
    }

    @Test
    void upsertTradeSignature_shouldSyncSigners_onUpdate() {
        // Signers existentes en base de datos (antes de la actualización)
        TradeSigner existingSigner1 = getTradeSigner("S1","Signer Uno", "111");
        TradeSigner existingSigner2 = getTradeSigner("S2","Signer Dos", "222");

        TradeSignature foundSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        foundSignature.setTradeSignerList(List.of(existingSigner1, existingSigner2));

        // Request con:
        // - S1 (modificado),
        // - S3 (nuevo),
        // - S2 eliminado (no viene en request)
        TradeSignerRequest updatedSigner1 = getTradeSignerRequest("S1","Signer Uno Modificado", "999");
        TradeSignerRequest newSigner3 = getTradeSignerRequest("S3","Signer Tres", "333");

        TradeSignatureRequest request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);
        request.setSigners(List.of(updatedSigner1, newSigner3));
        request.setOriginId(null);

        // Simula búsqueda exitosa y guardado
        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.just(foundSignature));
        when(tradeSignatureRepositoryClient.save(any(TradeSignature.class))).thenAnswer(invocation -> {
            TradeSignature toSave = invocation.getArgument(0);
            // Devuelve el mismo objeto para validación
            return Mono.just(toSave);
        });

        TradeSignatureResponse response = tradeSignatureServiceImpl
                .createOrUpdateSignature(LocaleConstants.DEFAULT_LOCALE, LocaleConstants.ENTITY_0049, request)
                .block();

        assertNotNull(response);
        ArgumentCaptor<TradeSignature> captor = ArgumentCaptor.forClass(TradeSignature.class);
        verify(tradeSignatureRepositoryClient).save(captor.capture());
        TradeSignature saved = captor.getValue();

        // 1. Solo deben quedar los signers S1 (modificado) y S3 (nuevo)
        assertEquals(2, saved.getTradeSignerList().size());
        assertTrue(saved.getTradeSignerList().stream().anyMatch(s -> "S1".equals(s.getSignerId())));
        assertTrue(saved.getTradeSignerList().stream().anyMatch(s -> "S3".equals(s.getSignerId())));
        assertFalse(saved.getTradeSignerList().stream().anyMatch(s -> "S2".equals(s.getSignerId())));

        // 2. S1 debe estar actualizado
        TradeSigner s1 = saved.getTradeSignerList().stream().filter(s -> "S1".equals(s.getSignerId())).findFirst().orElseThrow();
        assertEquals("Signer Uno Modificado", s1.getName());
        assertEquals("999", s1.getDocumentNumber());

        // 3. S3 debe estar insertado correctamente
        TradeSigner s3 = saved.getTradeSignerList().stream().filter(s -> "S3".equals(s.getSignerId())).findFirst().orElseThrow();
        assertEquals("Signer Tres", s3.getName());
        assertEquals("333", s3.getDocumentNumber());
    }

    private TradeSigner getTradeSigner(String signerId, String name, String documentNumber) {
        return TradeSigner.builder()
                .signerId(signerId)
                .name(name)
                .documentNumber(documentNumber)
                .build();
    }

    private TradeSignerRequest getTradeSignerRequest(String signerId, String name, String documentNumber) {
        return TradeSignerRequest.builder()
                .signerId(signerId)
                .name(name)
                .document(SignerDocument.builder().number(documentNumber).build())
                .build();
    }

}

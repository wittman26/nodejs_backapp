package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
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

import java.util.List;
import java.util.Locale;

import static com.acelera.fx.digitalsignature.TestUtils.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TradeSignatureServiceImplTest {

    @InjectMocks
    private TradeSignatureServiceImpl tradeSignatureServiceImpl;

    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    @BeforeEach
    void setUp() {
        tradeSignatureServiceImpl = new TradeSignatureServiceImpl(tradeSignatureRepositoryClient);
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
                        LocaleConstants.ENTITY_HEADER,
                        getTradeSignatureRequest(INVALID_REQUEST))
        );
    }

    @Test
    void createOrUpdateSignature_shouldCreateNewTradeSignature_whenNotFound() {
        TradeSignatureRequest request = getTradeSignatureRequest(ONLY_ORIGIN_ID);
        TradeSignature savedSignature = TradeSignature.builder().tradeSignatureId(123L).originId(request.getOriginId()).build();

        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.empty());
        when(tradeSignatureRepositoryClient.save(any(TradeSignature.class))).thenReturn(Mono.just(savedSignature));

        TradeSignatureResponse response = tradeSignatureServiceImpl
                .createOrUpdateSignature(Locale.getDefault(), "BANK", request)
                .block();

        assertNotNull(response);
        assertEquals(123, response.getTradeSignatureId());
        verify(tradeSignatureRepositoryClient).save(any(TradeSignature.class));
    }

    @Test
    void createOrUpdateSignature_shouldUpdateTradeSignature_whenFound() {
        TradeSignatureRequest request = getTradeSignatureRequest(ONLY_TRADE_SIGNATURE_ID);
        TradeSignature foundSignature = TradeSignature.builder()
                .tradeSignatureId(request.getTradeSignatureId())
                .originId(999L)
                .validatedBo("EXISTING")
                .build();
        TradeSignature updatedSignature = TradeSignature.builder()
                .tradeSignatureId(request.getTradeSignatureId())
                .originId(999L)
                .validatedBo("EXISTING")
                .build();

        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.just(foundSignature));
        when(tradeSignatureRepositoryClient.save(any(TradeSignature.class))).thenReturn(Mono.just(updatedSignature));

        TradeSignatureResponse response = tradeSignatureServiceImpl
                .createOrUpdateSignature(Locale.getDefault(), "BANK", request)
                .block();

        assertNotNull(response);
        assertEquals(request.getTradeSignatureId().intValue(), response.getTradeSignatureId());
        verify(tradeSignatureRepositoryClient).save(any(TradeSignature.class));
    }

    @Test
    void upsertTradeSignature_shouldThrowException_whenOriginIdIsNullOnCreate() {
        TradeSignatureRequest request = getTradeSignatureRequest(ONLY_TRADE_SIGNATURE_ID);
        // Simula que el request no tiene originId y es un alta (no se encuentra en base)
        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.empty());

        // Forzar originId a null
        request.setOriginId(null);

        assertThrows(
                IllegalArgumentException.class,
                () -> tradeSignatureServiceImpl.createOrUpdateSignature(Locale.getDefault(), "BANK", request).block()
        );
    }

    @Test
    void upsertTradeSignature_shouldSetPendingAndEventProperly() {
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
                .createOrUpdateSignature(Locale.getDefault(), "BANK", request)
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
    void upsertTradeSignature_shouldSetTradeSignerListToEmptyIfNull() {
        TradeSignatureRequest request = getTradeSignatureRequest(ONLY_ORIGIN_ID);
        request.setSigners(null);

        TradeSignature savedSignature = TradeSignature.builder()
                .tradeSignatureId(789L)
                .originId(request.getOriginId())
                .build();

        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.empty());
        when(tradeSignatureRepositoryClient.save(any(TradeSignature.class))).thenReturn(Mono.just(savedSignature));

        TradeSignatureResponse response = tradeSignatureServiceImpl
                .createOrUpdateSignature(Locale.getDefault(), "BANK", request)
                .block();

        assertNotNull(response);
        assertEquals(789, response.getTradeSignatureId());
        ArgumentCaptor<TradeSignature> captor = ArgumentCaptor.forClass(TradeSignature.class);
        verify(tradeSignatureRepositoryClient).save(captor.capture());
        TradeSignature toSave = captor.getValue();
        assertNotNull(toSave.getTradeSignerList());
        assertTrue(toSave.getTradeSignerList().isEmpty());
    }

        @Test
        void upsertTradeSignature_shouldSyncSigners_onUpdate() {
        // Signers existentes en base de datos (antes de la actualización)
        TradeSigner existingSigner1 = TradeSigner.builder()
                .signerId("S1")
                .name("Signer Uno")
                .documentNumber("111")
                .build();
        TradeSigner existingSigner2 = TradeSigner.builder()
                .signerId("S2")
                .name("Signer Dos")
                .documentNumber("222")
                .build();

        TradeSignature foundSignature = TradeSignature.builder()
                .tradeSignatureId(100L)
                .originId(200L)
                .validatedBo("EXISTING")
                .tradeSignerList(List.of(existingSigner1, existingSigner2))
                .build();

        // Request con: 
        // - S1 (modificado), 
        // - S3 (nuevo), 
        // - S2 eliminado (no viene en request)
        TradeSignerRequest updatedSigner1 = TradeSignerRequest.builder()
                .signerId("S1")
                .name("Signer Uno Modificado")
                .document(SignerDocument.builder().number("999").build())
                .build();
        TradeSignerRequest newSigner3 = TradeSignerRequest.builder()
                .signerId("S3")
                .name("Signer Tres")
                .document(SignerDocument.builder().number("333").build())
                .build();

        TradeSignatureRequest request = TradeSignatureRequest.builder()
                .tradeSignatureId(100L)
                .signers(List.of(updatedSigner1, newSigner3))
                .build();

        // Simula búsqueda exitosa y guardado
        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.just(foundSignature));
        when(tradeSignatureRepositoryClient.save(any(TradeSignature.class))).thenAnswer(invocation -> {
                TradeSignature toSave = invocation.getArgument(0);
                // Devuelve el mismo objeto para validación
                return Mono.just(toSave);
        });

        TradeSignatureResponse response = tradeSignatureServiceImpl
                .createOrUpdateSignature(Locale.getDefault(), "BANK", request)
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
}
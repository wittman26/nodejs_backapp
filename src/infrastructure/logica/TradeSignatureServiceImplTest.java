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
}
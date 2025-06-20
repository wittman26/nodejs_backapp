package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureMapper;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignatureDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeSignatureServiceSaveImplTest {

    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    @InjectMocks
    private TradeSignatureServiceSaveImpl tradeSignatureServiceSaveImpl;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    private static final String ENTITY = "ENTITY_0049";
    private static final Locale LOCALE = Locale.getDefault();

    @Test
    void createOrUpdateSignature_shouldCreateWhenNotFound() {
        TradeSignatureDto dto = PODAM_FACTORY.manufacturePojo(TradeSignatureDto.class);
        TradeSignature saved = PODAM_FACTORY.manufacturePojo(TradeSignature.class);

        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.empty());
        when(tradeSignatureRepositoryClient.save(any())).thenReturn(Mono.just(saved));

        TradeSignature result = tradeSignatureServiceSaveImpl
                .createOrUpdateSignature(LOCALE, ENTITY, dto)
                .block();

        assertNotNull(result);
        verify(tradeSignatureRepositoryClient).find(any());
        verify(tradeSignatureRepositoryClient).save(any());
    }

    @Test
    void createOrUpdateSignature_shouldUpdateWhenFound() {
        TradeSignatureDto dto = PODAM_FACTORY.manufacturePojo(TradeSignatureDto.class);
        TradeSignature found = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        TradeSignature updated = PODAM_FACTORY.manufacturePojo(TradeSignature.class);

        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.just(found));
        when(tradeSignatureRepositoryClient.save(any())).thenReturn(Mono.just(updated));

        TradeSignature result = tradeSignatureServiceSaveImpl
                .createOrUpdateSignature(LOCALE, ENTITY, dto)
                .block();

        assertNotNull(result);
        verify(tradeSignatureRepositoryClient).find(any());
        verify(tradeSignatureRepositoryClient).save(any());
    }

    @Test
    void upsertTradeSignature_shouldReturnErrorWhenExpedientIdPresent() {
        TradeSignatureDto dto = PODAM_FACTORY.manufacturePojo(TradeSignatureDto.class);
        TradeSignature found = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        found.setExpedientId(123L); // Simula que ya tiene expedientId

        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.just(found));

        Exception ex = assertThrows(CustomErrorException.class, () -> {
            tradeSignatureServiceSaveImpl
                    .createOrUpdateSignature(LOCALE, ENTITY, dto)
                    .block();
        });
        assertTrue(ex.getMessage().contains("expedientId"));
    }

    @Test
    void upsertTradeSignature_shouldReturnErrorWhenOriginIdIsNullOnCreate() {
        TradeSignatureDto dto = PODAM_FACTORY.manufacturePojo(TradeSignatureDto.class);
        dto.setOriginId(null);

        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.empty());

        Exception ex = assertThrows(CustomErrorException.class, () -> {
            tradeSignatureServiceSaveImpl
                    .createOrUpdateSignature(LOCALE, ENTITY, dto)
                    .block();
        });
        assertTrue(ex.getMessage().contains("NO_TRANSFER_ID_FOUND"));
    }
}

/**************/ */

package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureMapper;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignatureDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.MessageSource;
import reactor.core.publisher.Mono;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeSignatureServiceSaveImplTest {

    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    @InjectMocks
    private TradeSignatureServiceSaveImpl tradeSignatureServiceSaveImpl;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    private static final String ENTITY = "ENTITY_0049";
    private static final Locale LOCALE = Locale.getDefault();

    @Test
    void createOrUpdateSignature_shouldCreateWhenNotFound() {
        TradeSignatureDto dto = PODAM_FACTORY.manufacturePojo(TradeSignatureDto.class);
        TradeSignature saved = PODAM_FACTORY.manufacturePojo(TradeSignature.class);

        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.empty());
        when(tradeSignatureRepositoryClient.save(any())).thenReturn(Mono.just(saved));

        TradeSignature result = tradeSignatureServiceSaveImpl
                .createOrUpdateSignature(LOCALE, ENTITY, dto)
                .block();

        assertNotNull(result);
        verify(tradeSignatureRepositoryClient).find(any());
        verify(tradeSignatureRepositoryClient).save(any());
    }

    @Test
    void createOrUpdateSignature_shouldUpdateWhenFound() {
        TradeSignatureDto dto = PODAM_FACTORY.manufacturePojo(TradeSignatureDto.class);
        TradeSignature found = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        TradeSignature updated = PODAM_FACTORY.manufacturePojo(TradeSignature.class);

        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.just(found));
        when(tradeSignatureRepositoryClient.save(any())).thenReturn(Mono.just(updated));

        TradeSignature result = tradeSignatureServiceSaveImpl
                .createOrUpdateSignature(LOCALE, ENTITY, dto)
                .block();

        assertNotNull(result);
        verify(tradeSignatureRepositoryClient).find(any());
        verify(tradeSignatureRepositoryClient).save(any());
    }

    @Test
    void upsertTradeSignature_shouldReturnErrorWhenExpedientIdPresent() {
        TradeSignatureDto dto = PODAM_FACTORY.manufacturePojo(TradeSignatureDto.class);
        TradeSignature found = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        found.setExpedientId(123L); // Simula que ya tiene expedientId

        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.just(found));

        try (MockedStatic<com.acelera.locale.MessageSourceHolder> mocked = mockStatic(com.acelera.locale.MessageSourceHolder.class)) {
            MessageSource mockSource = mock(MessageSource.class);
            when(mockSource.getMessage(any(), any(), any(Locale.class))).thenReturn("expedientId error");
            mocked.when(com.acelera.locale.MessageSourceHolder::getMessageSource).thenReturn(mockSource);

            Exception ex = assertThrows(CustomErrorException.class, () -> {
                tradeSignatureServiceSaveImpl
                        .createOrUpdateSignature(LOCALE, ENTITY, dto)
                        .block();
            });
            assertTrue(ex.getMessage().contains("expedientId"));
        }
    }

    @Test
    void upsertTradeSignature_shouldReturnErrorWhenOriginIdIsNullOnCreate() {
        TradeSignatureDto dto = PODAM_FACTORY.manufacturePojo(TradeSignatureDto.class);
        dto.setOriginId(null);

        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.empty());

        try (MockedStatic<com.acelera.locale.MessageSourceHolder> mocked = mockStatic(com.acelera.locale.MessageSourceHolder.class)) {
            MessageSource mockSource = mock(MessageSource.class);
            when(mockSource.getMessage(any(), any(), any(Locale.class))).thenReturn("NO_TRANSFER_ID_FOUND");
            mocked.when(com.acelera.locale.MessageSourceHolder::getMessageSource).thenReturn(mockSource);

            Exception ex = assertThrows(CustomErrorException.class, () -> {
                tradeSignatureServiceSaveImpl
                        .createOrUpdateSignature(LOCALE, ENTITY, dto)
                        .block();
            });
            assertTrue(ex.getMessage().contains("NO_TRANSFER_ID_FOUND"));
        }
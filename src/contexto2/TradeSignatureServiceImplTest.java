package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.fx.digitalsignature.domain.TradeSignatureDomainService;
import com.acelera.broker.fx.domain.dto.request.TradeSignatureRequest;
import com.acelera.broker.fx.domain.dto.response.TradeSignatureResponse;
import com.acelera.locale.LocaleConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TradeSignatureServiceImplTest {

    @Mock
    private TradeSignatureDomainService tradeSignatureDomainService;

    @InjectMocks
    private TradeSignatureServiceImpl tradeSignatureServiceImpl;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    @Test
    void createOrUpdateSignature_shouldDelegateToDomainService() {
        TradeSignatureRequest request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);
        TradeSignatureResponse expectedResponse =  PODAM_FACTORY.manufacturePojo(TradeSignatureResponse.class);
        TradeSignature expectedFindResponse = PODAM_FACTORY.manufacturePojo(TradeSignature.class);

        when(tradeSignatureDomainService.findTradeSignature(any(), any()))
                .thenReturn(Mono.just(expectedFindResponse));

        when(tradeSignatureDomainService.upsertTradeSignature(any(), any(), any()))
                .thenReturn(Mono.just(expectedResponse));

        TradeSignatureResponse response = tradeSignatureServiceImpl
                .createOrUpdateSignature(LocaleConstants.DEFAULT_LOCALE, LocaleConstants.ENTITY_0049, request)
                .block();

        assertEquals(expectedResponse, response);
        verify(tradeSignatureDomainService).validateCreateOrUpdateParams(request);
        verify(tradeSignatureDomainService).findTradeSignature(request, LocaleConstants.ENTITY_0049);
    }

    @Test
    void createOrUpdateSignature_shouldCallUpsertToSaveWhenNotFound() {
        TradeSignatureRequest request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);
        TradeSignatureResponse expectedResponse =  PODAM_FACTORY.manufacturePojo(TradeSignatureResponse.class);

        doNothing().when(tradeSignatureDomainService).validateCreateOrUpdateParams(request);

        when(tradeSignatureDomainService.findTradeSignature(request, LocaleConstants.ENTITY_0049))
                .thenReturn(Mono.empty());

        when(tradeSignatureDomainService.upsertTradeSignature(null, request, LocaleConstants.ENTITY_0049))
                .thenReturn(Mono.just(expectedResponse));

        TradeSignatureResponse result = tradeSignatureServiceImpl
                .createOrUpdateSignature(LocaleConstants.DEFAULT_LOCALE, LocaleConstants.ENTITY_0049, request)
                .block();

        assertEquals(expectedResponse, result);
        verify(tradeSignatureDomainService).validateCreateOrUpdateParams(request);
        verify(tradeSignatureDomainService).findTradeSignature(request, LocaleConstants.ENTITY_0049);
        verify(tradeSignatureDomainService).upsertTradeSignature(null, request, LocaleConstants.ENTITY_0049);
    }


    @Test
    void createOrUpdateSignature_shouldCallUpsertToUpdateWhenFound() {
        TradeSignatureRequest request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);
        request.setTradeSignatureId(1L);
        TradeSignatureResponse expectedResponse =  PODAM_FACTORY.manufacturePojo(TradeSignatureResponse.class);
        TradeSignature tradeSignatureFound = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        tradeSignatureFound.setTradeSignatureId(1L);
        expectedResponse.setTradeSignatureId(1L);

        doNothing().when(tradeSignatureDomainService).validateCreateOrUpdateParams(request);

        when(tradeSignatureDomainService.findTradeSignature(request, LocaleConstants.ENTITY_0049))
                .thenReturn(Mono.just(tradeSignatureFound));

        when(tradeSignatureDomainService.upsertTradeSignature(tradeSignatureFound, request, LocaleConstants.ENTITY_0049))
                .thenReturn(Mono.just(expectedResponse));

        TradeSignatureResponse result = tradeSignatureServiceImpl
                .createOrUpdateSignature(LocaleConstants.DEFAULT_LOCALE, LocaleConstants.ENTITY_0049, request)
                .block();

        assertEquals(expectedResponse, result);
        assertNotNull(result);
        assertEquals(request.getTradeSignatureId().intValue(), result.getTradeSignatureId());
        verify(tradeSignatureDomainService).validateCreateOrUpdateParams(request);
        verify(tradeSignatureDomainService).findTradeSignature(request, LocaleConstants.ENTITY_0049);
        verify(tradeSignatureDomainService).upsertTradeSignature(tradeSignatureFound, request, LocaleConstants.ENTITY_0049);
    }
}

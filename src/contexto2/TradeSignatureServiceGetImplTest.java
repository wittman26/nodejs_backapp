package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.*;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.fx.db.domain.port.ViewTradeSignatureRepositoryClient;
import com.acelera.broker.shared.domain.PageDto;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureParameterDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeSignatureServiceGetImplTest {

    @Mock
    private TradeSignatureRepositoryClient repository;

    @Mock
    private ViewTradeSignatureRepositoryClient viewRepository;

    @Mock
    private TradeSignerHelper tradeSignerHelper;

    @InjectMocks
    private TradeSignatureServiceGetImpl impl;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    @BeforeEach
    void setUp() {
        // No-op, mocks and injects handled by MockitoExtension
    }

    @Test
    void getTradeSignatureResponse_shouldReturnCombinedResponse() {
        Long tradeSignatureId = 1L;
        GetTradeSignatureParameterDto request = PODAM_FACTORY.manufacturePojo(GetTradeSignatureParameterDto.class);

        var req = ViewTradeSignatureExpedientFindByFilterRequest.builder().tradeSignatureId(1L).build();
        when(viewRepository.findByFilter(req))
                .thenReturn(Mono.just(new PageDto<ViewTradeSignatureExpedient>()));

        when(viewRepository.findTradeSignerViewDocument(req.getTradeSignatureId()))
                .thenReturn(Mono.just(List.of()));

        Mono<GetTradeSignatureDto> result = impl.getTradeSignatureResponse(tradeSignatureId);
        assertNotNull(result.block());
        verify(viewRepository).findByFilter(req);
        verify(viewRepository).findTradeSignerViewDocument(tradeSignatureId);
    }

    @Test
    void getTradeSignature_shouldReturnCombinedDto_whenFound() {
        GetTradeSignatureParameterDto paramDto = PODAM_FACTORY.manufacturePojo(GetTradeSignatureParameterDto.class);
        TradeSignature tradeSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        GetTradeSignatureDto expectedDto = PODAM_FACTORY.manufacturePojo(GetTradeSignatureDto.class);
        TradeSignatureFindRequest req = PODAM_FACTORY.manufacturePojo(TradeSignatureFindRequest.class);

        // Simula que se encuentra la firma
        when(repository.find(req)).thenReturn(Mono.just(tradeSignature));
        // Simula la respuesta combinada
        var spyService = spy(impl);
        doReturn(Mono.just(expectedDto)).when(spyService).getTradeSignatureResponse(tradeSignature.getTradeSignatureId());

        GetTradeSignatureDto result = impl.getTradeSignature(Locale.getDefault(), "ENTITY", paramDto).block();
        assertNotNull(result);
        verify(repository).find(any());
    }

    @Test
    void mapSignersWithColour_shouldGroupAndSetColour() {
        TradeSignerDocumentStatusView doc1 = new TradeSignerDocumentStatusView();
        doc1.setSignerId("A");
        doc1.setSignedDoc("Y");
        TradeSignerDocumentStatusView doc2 = new TradeSignerDocumentStatusView();
        doc2.setSignerId("A");
        doc2.setSignedDoc("N");

        when(tradeSignerHelper.getSignerColour(any())).thenReturn("YELLOW");

        List<TradeSignerDto> result = impl.mapSignersWithColour(List.of(doc1, doc2));
        assertEquals(1, result.size());
        assertEquals("YELLOW", result.get(0).getSignerColour());
    }
}
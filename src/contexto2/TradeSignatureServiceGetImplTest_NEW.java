package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedient;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedientFindByFilterRequest;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.fx.db.domain.port.ViewTradeSignatureRepositoryClient;
import com.acelera.broker.shared.domain.PageDto;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureParameterDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.locale.LocaleConstants;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TradeSignatureServiceGetImplTest {

    @Mock
    private TradeSignerHelper tradeSignerHelper;

    @Mock
    private TradeSignatureRepositoryClient repository;

    @Mock
    private ViewTradeSignatureRepositoryClient viewRepository;

    @InjectMocks
    private TradeSignatureServiceGetImpl impl;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    private static final String ENTITY = LocaleConstants.ENTITY_0049;
    private static final Locale LOCALE = LocaleConstants.DEFAULT_LOCALE;

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
        assertEquals("YELLOW", result.getFirst().getSignerColour());
    }
}
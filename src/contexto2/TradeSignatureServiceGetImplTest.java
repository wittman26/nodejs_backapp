package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedient;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedientFindByFilterRequest;
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
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TradeSignatureServiceGetImplTest {

    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    @Mock
    private ViewTradeSignatureRepositoryClient viewTradeSignatureRepositoryClient;

    @Mock
    private TradeSignerHelper tradeSignerHelper;

    @InjectMocks
    private TradeSignatureServiceGetImpl service;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    @BeforeEach
    void setUp() {
        // No-op, mocks and injects handled by MockitoExtension
    }

    @Test
    void getTradeSignature_shouldReturnCombinedDto_whenFound() {
        GetTradeSignatureParameterDto paramDto = PODAM_FACTORY.manufacturePojo(GetTradeSignatureParameterDto.class);
        TradeSignature tradeSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        GetTradeSignatureDto expectedDto = PODAM_FACTORY.manufacturePojo(GetTradeSignatureDto.class);

        // Simula que se encuentra la firma
        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.just(tradeSignature));
        // Simula la respuesta combinada
        doReturn(Mono.just(expectedDto)).when(service).getTradeSignatureResponse(tradeSignature.getTradeSignatureId());

        GetTradeSignatureDto result = service.getTradeSignature(Locale.getDefault(), "ENTITY", paramDto).block();
        assertNotNull(result);
        verify(tradeSignatureRepositoryClient).find(any());
    }

    @Test
    void getTradeSignature_shouldReturnCombinedDto_whenNotFound() {
        GetTradeSignatureParameterDto paramDto = PODAM_FACTORY.manufacturePojo(GetTradeSignatureParameterDto.class);
        GetTradeSignatureDto expectedDto = PODAM_FACTORY.manufacturePojo(GetTradeSignatureDto.class);

        // Simula que no se encuentra la firma
        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.empty());
        doReturn(Mono.just(expectedDto)).when(service).getTradeSignatureResponse(paramDto.getTradeSignatureId());

        GetTradeSignatureDto result = service.getTradeSignature(Locale.getDefault(), "ENTITY", paramDto).block();
        assertNotNull(result);
        verify(tradeSignatureRepositoryClient).find(any());
    }

    @Test
    void getTradeSignatureResponse_shouldReturnHeaderAndSigners() {
        Long tradeSignatureId = 1L;
        ViewTradeSignatureExpedient expedient = PODAM_FACTORY.manufacturePojo(ViewTradeSignatureExpedient.class);
        PageDto<ViewTradeSignatureExpedient> page = new PageDto<>();
        page.setContent(List.of(expedient));
        GetTradeSignatureDto headerDto = PODAM_FACTORY.manufacturePojo(GetTradeSignatureDto.class);
        List<TradeSignerDto> signers = PODAM_FACTORY.manufacturePojo(List.class, TradeSignerDto.class);

        when(viewTradeSignatureRepositoryClient.findByFilter(any(ViewTradeSignatureExpedientFindByFilterRequest.class)))
                .thenReturn(Mono.just(page));
        when(viewTradeSignatureRepositoryClient.findTradeSignerViewDocument(tradeSignatureId))
                .thenReturn(Mono.just(List.of()));
        // Simula el mapeo de header y signers
        try (var mapperMock = org.mockito.Mockito.mockStatic(
                com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureViewMapper.class)) {
            mapperMock.when(() -> com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureViewMapper.INSTANCE.fromDataToGetTradeSignatureDto(expedient))
                    .thenReturn(headerDto);

            // Simula el mapeo de signers
            doReturn(signers).when(service).mapSignersWithColour(any());

            GetTradeSignatureDto result = service.getTradeSignatureResponse(tradeSignatureId).block();
            assertNotNull(result);
            assertEquals(headerDto, result);
        }
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

        List<TradeSignerDto> result = service.mapSignersWithColour(List.of(doc1, doc2));
        assertEquals(1, result.size());
        assertEquals("YELLOW", result.get(0).getSignerColour());
    }
}
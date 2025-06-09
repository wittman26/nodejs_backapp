import com.acelera.broker.fx.db.domain.dto.*;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.fx.db.domain.port.TradeSignatureViewRepositoryClient;
import com.acelera.fx.digitalsignature.domain.TradeSignatureDomainService;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.infrastructure.request.GetTradeSignatureRequestParameter;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.GetTradeSignatureResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignatureResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignersResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradeSignatureDomainServiceTest {

    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;
    @Mock
    private TradeSignatureViewRepositoryClient tradeSignatureViewRepositoryClient;
    @Mock
    private TradeSignerHelper tradeSignerHelper;

    @InjectMocks
    private TradeSignatureDomainService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TradeSignatureDomainService(
                tradeSignatureRepositoryClient,
                tradeSignatureViewRepositoryClient,
                tradeSignerHelper
        );
    }

    @Test
    void validateCreateOrUpdateParams_shouldThrowIfBothNullOrBothNotNull() {
        TradeSignatureRequest req = new TradeSignatureRequest();
        req.setTradeSignatureId(null);
        req.setOriginId(null);
        assertThrows(IllegalArgumentException.class, () -> service.validateCreateOrUpdateParams(req));

        req.setTradeSignatureId(1L);
        req.setOriginId(2L);
        assertThrows(IllegalArgumentException.class, () -> service.validateCreateOrUpdateParams(req));
    }

    @Test
    void findTradeSignature_shouldCallRepository() {
        TradeSignatureRequest req = new TradeSignatureRequest();
        req.setTradeSignatureId(1L);
        TradeSignatureFindRequest findRequest = new TradeSignatureFindRequest();
        findRequest.setTradeSignatureId(1L);
        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.just(new TradeSignature()));

        Mono<TradeSignature> result = service.findTradeSignature(req, "ENTITY");
        assertNotNull(result.block());
        verify(tradeSignatureRepositoryClient).find(any());
    }

    @Test
    void upsertTradeSignature_shouldSaveAndReturnResponse() {
        TradeSignatureRequest req = new TradeSignatureRequest();
        req.setProductId("FW");
        req.setSigners(List.of());
        TradeSignature tradeSignature = new TradeSignature();
        tradeSignature.setTradeSignatureId(1L);

        when(tradeSignatureRepositoryClient.save(any())).thenReturn(Mono.just(tradeSignature));

        Mono<TradeSignatureResponse> result = service.upsertTradeSignature(null, req, "ENTITY");
        TradeSignatureResponse response = result.block();
        assertNotNull(response);
        assertEquals(1, response.getTradeSignatureId());
        verify(tradeSignatureRepositoryClient).save(any());
    }

    @Test
    void getTradeSignatureResponse_shouldReturnCombinedResponse() {
        Long tradeSignatureId = 1L;
        GetTradeSignatureResponse header = new GetTradeSignatureResponse();
        List<TradeSignersResponse> signers = List.of(new TradeSignersResponse());

        when(tradeSignatureViewRepositoryClient.findTradeSignatureViewExpedient(tradeSignatureId))
                .thenReturn(Mono.just(new TradeSignatureExpedientView()));
        when(tradeSignatureViewRepositoryClient.findTradeSignerViewDocument(tradeSignatureId))
                .thenReturn(Mono.just(List.of()));
        // Mockea el mapper estático si es necesario

        Mono<GetTradeSignatureResponse> result = service.getTradeSignatureResponse(tradeSignatureId);
        assertNotNull(result.block());
        verify(tradeSignatureViewRepositoryClient).findTradeSignatureViewExpedient(tradeSignatureId);
        verify(tradeSignatureViewRepositoryClient).findTradeSignerViewDocument(tradeSignatureId);
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

        List<TradeSignersResponse> result = service.mapSignersWithColour(List.of(doc1, doc2));
        assertEquals(1, result.size());
        assertEquals("YELLOW", result.get(0).getSignerColour());
    }
}



import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureExpedientView;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.fx.db.domain.port.TradeSignatureViewRepositoryClient;
import com.acelera.fx.digitalsignature.domain.TradeSignatureDomainService;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.infrastructure.request.GetTradeSignatureRequestParameter;
import com.acelera.fx.digitalsignature.infrastructure.response.GetTradeSignatureResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignersResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class TradeSignatureDomainServiceTest {

    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;
    @Mock
    private TradeSignatureViewRepositoryClient tradeSignatureViewRepositoryClient;
    @Mock
    private TradeSignerHelper tradeSignerHelper;

    @InjectMocks
    private TradeSignatureDomainService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new TradeSignatureDomainService(
                tradeSignatureRepositoryClient,
                tradeSignatureViewRepositoryClient,
                tradeSignerHelper
        );
    }

    // --- validateGetParams ---

    @Test
    void validateGetParams_shouldNotThrow_whenValid() {
        GetTradeSignatureRequestParameter req = new GetTradeSignatureRequestParameter();
        req.setTradeSignatureId(1L);
        req.setOriginId(null);
        req.setOrigin(null);
        assertDoesNotThrow(() -> service.validateGetParams(req));
    }

    @Test
    void validateGetParams_shouldThrow_whenBothNullOrBothNotNull() {
        GetTradeSignatureRequestParameter req = new GetTradeSignatureRequestParameter();
        req.setTradeSignatureId(null);
        req.setOriginId(null);
        req.setOrigin(null);
        assertThrows(IllegalArgumentException.class, () -> service.validateGetParams(req));

        req.setTradeSignatureId(1L);
        req.setOriginId(2L);
        req.setOrigin("ORIGIN");
        assertThrows(IllegalArgumentException.class, () -> service.validateGetParams(req));
    }

    @Test
    void validateGetParams_shouldThrow_whenOriginIdAndOriginMismatch() {
        GetTradeSignatureRequestParameter req = new GetTradeSignatureRequestParameter();
        req.setTradeSignatureId(null);
        req.setOriginId(2L);
        req.setOrigin(null);
        assertThrows(IllegalArgumentException.class, () -> service.validateGetParams(req));
    }

    // --- getTradeSignatureResponse ---

    @Test
    void getTradeSignatureResponse_shouldReturnCombinedResponse() {
        Long tradeSignatureId = 1L;
        TradeSignatureExpedientView expedientView = new TradeSignatureExpedientView();
        GetTradeSignatureResponse header = new GetTradeSignatureResponse();
        List<TradeSignersResponse> signers = List.of(new TradeSignersResponse());

        // Mock mappers y repositorios
        when(tradeSignatureViewRepositoryClient.findTradeSignatureViewExpedient(tradeSignatureId))
                .thenReturn(Mono.just(expedientView));
        // Simula el mapeo estático
        try (MockedStatic<com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureViewMapper> mapperMock =
                     mockStatic(com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureViewMapper.class)) {
            mapperMock.when(() -> com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureViewMapper.INSTANCE.toGetTradeSignatureResponse(expedientView))
                    .thenReturn(header);

            when(tradeSignatureViewRepositoryClient.findTradeSignerViewDocument(tradeSignatureId))
                    .thenReturn(Mono.just(List.of()));
            // Simula el mapeo de signers
            doReturn(signers).when(service).mapSignersWithColour(any());

            Mono<GetTradeSignatureResponse> result = service.getTradeSignatureResponse(tradeSignatureId);
            GetTradeSignatureResponse response = result.block();
            assertNotNull(response);
        }
    }

    // --- mapSignersWithColour ---

    @Test
    void mapSignersWithColour_shouldGroupAndSetColour() {
        TradeSignerDocumentStatusView doc1 = new TradeSignerDocumentStatusView();
        doc1.setSignerId("A");
        doc1.setSignedDoc("Y");
        TradeSignerDocumentStatusView doc2 = new TradeSignerDocumentStatusView();
        doc2.setSignerId("A");
        doc2.setSignedDoc("N");

        when(tradeSignerHelper.getSignerColour(any())).thenReturn("YELLOW");

        List<TradeSignersResponse> result = service.mapSignersWithColour(List.of(doc1, doc2));
        assertEquals(1, result.size());
        assertEquals("YELLOW", result.get(0).getSignerColour());
    }
}
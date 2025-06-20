package com.acelera.fx.digitalsignature.domain;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.ViewTradeSignatureExpedient;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.fx.db.domain.port.ViewTradeSignatureRepositoryClient;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.broker.fx.domain.dto.request.GetTradeSignatureRequestParameter;
import com.acelera.broker.fx.domain.dto.request.TradeSignatureRequest;
import com.acelera.broker.fx.domain.dto.response.GetTradeSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.TradeSignatureResponse;
import com.acelera.broker.fx.domain.dto.response.TradeSignersResponse;
import com.acelera.locale.LocaleConstants;
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

import static com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper.ERROR_MESSAGE_NO_TRANSFER_ID_FOUND;
import static com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper.SIGNER_COLOUR_YELLOW;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
public class TradeSignatureDomainServiceTest {

    @InjectMocks
    private TradeSignatureDomainService service;


    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    @Mock
    private ViewTradeSignatureRepositoryClient viewTradeSignatureRepositoryClient;

    @Mock
    private TradeSignerHelper tradeSignerHelper;


    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();


    @BeforeEach
    void setUp() {
        openMocks(this);
        service = new TradeSignatureDomainService(
                tradeSignatureRepositoryClient,
          viewTradeSignatureRepositoryClient,
                tradeSignerHelper);
    }

    /**
     * Validates that originId or tradeSignatureId is sent on Request but not both
     */
    @Test
    void validateCreateOrUpdateParams_shouldThrowException_whenBothIdsPresentOrAbsent() {
        TradeSignatureRequest request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);
        assertThrows(
                IllegalArgumentException.class,
                () -> service.validateCreateOrUpdateParams(request)
        );
    }

    @Test
    void findTradeSignature_shouldCallRepository() {
        TradeSignatureRequest request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);

        when(tradeSignatureRepositoryClient.find(any())).thenReturn(Mono.just(new TradeSignature()));

        Mono<TradeSignature> result = service.findTradeSignature(request, LocaleConstants.ENTITY_0049);
        assertNotNull(result.block());
        verify(tradeSignatureRepositoryClient).find(any());
    }

    @Test
    void findTradeSignature_shouldReturnTradeSignature() {
        TradeSignatureRequest request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);
        TradeSignature tradeSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);

        when(tradeSignatureRepositoryClient.find(any(TradeSignatureFindRequest.class))).thenReturn(Mono.just(tradeSignature));

        Mono<TradeSignature> result = service.findTradeSignature(request,  LocaleConstants.ENTITY_0049);

        TradeSignature response = result.block();
        assertNotNull(response);
        assertEquals(tradeSignature.getTradeSignatureId(), response.getTradeSignatureId());
        verify(tradeSignatureRepositoryClient).find(any(TradeSignatureFindRequest.class));
    }


    @Test
    void upsertTradeSignature_shouldSaveAndReturnResponse() {
        TradeSignatureRequest request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);
        request.setTradeSignatureId(null);
        TradeSignature tradeSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        tradeSignature.setTradeSignatureId(1L);

        when(tradeSignatureRepositoryClient.save(any())).thenReturn(Mono.just(tradeSignature));

        Mono<TradeSignatureResponse> result = service.upsertTradeSignature(null, request,  LocaleConstants.ENTITY_0049);
        TradeSignatureResponse response = result.block();
        assertNotNull(response);
        assertEquals(1, response.getTradeSignatureId());
        verify(tradeSignatureRepositoryClient).save(any());
    }

    @Test
    void upsertTradeSignature_shouldThrowException_whenTradeSignatureNotFoundAndOriginIdIsNull() {

        TradeSignatureRequest request = PODAM_FACTORY.manufacturePojo(TradeSignatureRequest.class);
        request.setOriginId(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> service.upsertTradeSignature(null, request, LocaleConstants.ENTITY_0049)
        );
        assertEquals(ERROR_MESSAGE_NO_TRANSFER_ID_FOUND, ex.getMessage());
    }

    // --- validateGetParams ---

    @Test
    void validateGetParams_shouldNotThrow_whenValid() {
        GetTradeSignatureRequestParameter req = GetTradeSignatureRequestParameter.builder()
                .tradeSignatureId(1L)
                .originId(null)
                .origin(null).build();
        assertDoesNotThrow(() -> service.validateGetParams(req));
    }

    @Test
    void validateGetParams_shouldThrow_whenBothNullOrBothNotNull() {
        GetTradeSignatureRequestParameter req = GetTradeSignatureRequestParameter.builder()
                .tradeSignatureId(null)
                .originId(null)
                .origin(null).build();
        assertThrows(IllegalArgumentException.class, () -> service.validateGetParams(req));

        req.setTradeSignatureId(1L);
        req.setOriginId(2L);
        req.setOrigin("TRADE");
        assertThrows(IllegalArgumentException.class, () -> service.validateGetParams(req));
    }

    @Test
    void validateGetParams_shouldThrow_whenOriginIdAndOriginMismatch() {
        GetTradeSignatureRequestParameter req = GetTradeSignatureRequestParameter.builder()
                .tradeSignatureId(null)
                .originId(1L)
                .origin(null).build();
        assertThrows(IllegalArgumentException.class, () -> service.validateGetParams(req));
    }

    // --- getTradeSignatureResponse ---

    @Test
    void getTradeSignatureResponse_shouldReturnCombinedResponse() {
        Long tradeSignatureId = 1L;

        when(viewTradeSignatureRepositoryClient.findByFilter(tradeSignatureId))
                .thenReturn(Mono.just(new ViewTradeSignatureExpedient()));
        when(viewTradeSignatureRepositoryClient.findTradeSignerViewDocument(tradeSignatureId))
                .thenReturn(Mono.just(List.of()));

        Mono<GetTradeSignatureResponse> result = service.getTradeSignatureResponse(tradeSignatureId);
        assertNotNull(result.block());
        verify(viewTradeSignatureRepositoryClient).findByFilter(tradeSignatureId);
        verify(viewTradeSignatureRepositoryClient).findTradeSignerViewDocument(tradeSignatureId);
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

        when(tradeSignerHelper.getSignerColour(any())).thenReturn(SIGNER_COLOUR_YELLOW);

        List<TradeSignersResponse> result = service.mapSignersWithColour(List.of(doc1, doc2));
        assertEquals(1, result.size());
        assertEquals(SIGNER_COLOUR_YELLOW, result.get(0).getSignerColour());
    }

}

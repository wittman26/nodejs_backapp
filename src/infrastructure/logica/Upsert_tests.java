package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.locale.LocaleConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.acelera.fx.digitalsignature.TestUtils.INVALID_REQUEST;
import static com.acelera.fx.digitalsignature.TestUtils.getTradeSignatureRequest;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
public class TradeSignatureServiceImplTest {

    @InjectMocks
    private TradeSignatureServiceImpl tradeSignatureServiceImpl;

    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;


    @BeforeEach
    void setUp() {
        openMocks(this);
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

}

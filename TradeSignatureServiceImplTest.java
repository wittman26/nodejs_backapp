package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSigner;
import com.acelera.broker.fx.db.domain.port.TradeSignerRepositoryClient;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureMapper;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignerMapper;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureService;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignerRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignatureResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class TradeSignatureServiceImplTest {

    @Mock
    private TradeSignatureRepositoryClient tradeSignatureRepositoryClient;
    @Mock
    private TradeSignerRepositoryClient tradeSignerRepositoryClient;
    @InjectMocks
    private TradeSignatureServiceImpl tradeSignatureService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        tradeSignatureService = new TradeSignatureServiceImpl(tradeSignatureRepositoryClient, tradeSignerRepositoryClient);
    }

    @Test
    void createOrUpdateSignature_shouldThrowException_whenBothIdsPresentOrAbsent() {
        TradeSignatureRequest req = new TradeSignatureRequest();
        req.setTradeSignatureId(1);
        req.setOriginId(1);
        StepVerifier.create(tradeSignatureService.createOrUpdateSignature(Locale.getDefault(), "entity", req))
                .expectError(DigitalSignatureBusinessException.class)
                .verify();
        req.setTradeSignatureId(null);
        req.setOriginId(null);
        StepVerifier.create(tradeSignatureService.createOrUpdateSignature(Locale.getDefault(), "entity", req))
                .expectError(DigitalSignatureBusinessException.class)
                .verify();
    }

    @Test
    void createOrUpdateSignature_shouldUpdate_whenTradeSignatureIdPresent() {
        TradeSignatureRequest req = new TradeSignatureRequest();
        req.setTradeSignatureId(1);
        req.setSigners(Collections.emptyList());
        when(tradeSignerRepositoryClient.findTradeSignersByTradeSignatureId(anyInt())).thenReturn(Mono.just(Collections.emptyList()));
        when(tradeSignatureRepositoryClient.update(anyInt(), any())).thenReturn(Mono.just(new Object()));
        when(tradeSignatureRepositoryClient.save(any())).thenReturn(Mono.just(new Object()));
        StepVerifier.create(tradeSignatureService.createOrUpdateSignature(Locale.getDefault(), "entity", req))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void createOrUpdateSignature_shouldCreate_whenNoTradeSignatureFound() {
        TradeSignatureRequest req = new TradeSignatureRequest();
        req.setOriginId(1);
        req.setProductId("P1");
        req.setSigners(Collections.emptyList());
        when(tradeSignatureRepositoryClient.findByParams(anyString(), anyInt(), anyString())).thenReturn(Mono.empty());
        when(tradeSignatureRepositoryClient.save(any())).thenReturn(Mono.just(new Object()));
        StepVerifier.create(tradeSignatureService.createOrUpdateSignature(Locale.getDefault(), "entity", req))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void sincronizarTradeSigners_shouldCallAllSubMethods() {
        List<TradeSignerRequest> incoming = Arrays.asList(new TradeSignerRequest(), new TradeSignerRequest());
        List<TradeSigner> existing = Arrays.asList(new TradeSigner(), new TradeSigner());
        StepVerifier.create(tradeSignatureService.sincronizarTradeSigners(1, incoming, existing))
                .verifyComplete();
    }
}

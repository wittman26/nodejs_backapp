package contexto4.app.tests;

package com.acelera.fx.digitalsignature.application.usecase.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import com.acelera.broker.fx.db.domain.dto.EventDisclaimer;
import com.acelera.broker.fx.db.domain.dto.OperationDisclaimer;
import com.acelera.broker.fx.db.domain.port.EventDisclaimerRepositoryClient;
import com.acelera.broker.fx.db.domain.port.OperationDisclaimerRepositoryClient;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateExpedientGetClausesUseCaseImplTest {

    @Mock
    private TradeSignerHelper tradeSignerHelper;

    @Mock
    private EventDisclaimerRepositoryClient eventDisclaimerRepositoryClient;

    @Mock
    private OperationDisclaimerRepositoryClient operationDisclaimerRepositoryClient;

    @InjectMocks
    private CreateExpedientGetClausesUseCaseImpl useCase;

    private final String entity = "0049";
    private final Long originId = 123L;
    private final String eventProductId = "EVENT_PROD";
    private final String tradeProductId = "TRADE_PROD";

    @BeforeEach
    void setUp() {
        when(tradeSignerHelper.isEventProduct(eventProductId)).thenReturn(true);
        when(tradeSignerHelper.isEventProduct(tradeProductId)).thenReturn(false);

        EventDisclaimer eventDisclaimer = EventDisclaimer.builder()
                .name("EVENT_CLAUSE")
                .content("Event clause content")
                .build();
        when(eventDisclaimerRepositoryClient.findByEntityAndEventId(any()))
                .thenReturn(Mono.just(eventDisclaimer));

        OperationDisclaimer operationDisclaimer = OperationDisclaimer.builder()
                .name("TRADE_CLAUSE")
                .content("Trade clause content")
                .build();
        when(operationDisclaimerRepositoryClient.findByEntityAndTradeId(any()))
                .thenReturn(Mono.just(operationDisclaimer));
    }

    @Test
    void obtainClauses_eventPath_success() {
        StepVerifier.create(useCase.obtainClauses(entity, originId, eventProductId))
                .expectNextMatches(clauses -> 
                    clauses.size() == 1 &&
                    clauses.get(0).getIdClause().equals("EVENT_CLAUSE") &&
                    clauses.get(0).getClauseContent().equals("Event clause content"))
                .verifyComplete();

        verify(eventDisclaimerRepositoryClient).findByEntityAndEventId(any());
    }

    @Test
    void obtainClauses_tradePath_success() {
        StepVerifier.create(useCase.obtainClauses(entity, originId, tradeProductId))
                .expectNextMatches(clauses -> 
                    clauses.size() == 1 &&
                    clauses.get(0).getIdClause().equals("TRADE_CLAUSE") &&
                    clauses.get(0).getClauseContent().equals("Trade clause content"))
                .verifyComplete();

        verify(operationDisclaimerRepositoryClient).findByEntityAndTradeId(any());
    }

    @Test
    void obtainClauses_eventPath_notFound() {
        when(eventDisclaimerRepositoryClient.findByEntityAndEventId(any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.obtainClauses(entity, originId, eventProductId))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Disclaimer no encontrado en ACELER.ACE_EVENT_DISCLAIMER: " + originId))
                .verify();
    }

    @Test
    void obtainClauses_tradePath_notFound() {
        when(operationDisclaimerRepositoryClient.findByEntityAndTradeId(any()))
                .thenReturn(Mono.empty());

        StepVerifier.create(useCase.obtainClauses(entity, originId, tradeProductId))
                .expectErrorMatches(e -> e instanceof RuntimeException &&
                        e.getMessage().equals("Disclaimer no encontrado en ACELER.ACE_OPERATION_DISCLAIMER: " + originId))
                .verify();
    }
}
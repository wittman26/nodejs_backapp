package com.acelera.fx.digitalsignature.application.usecase.impl;

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

import com.acelera.broker.fx.db.domain.dto.*;
import com.acelera.broker.fx.db.domain.port.*;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple4;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CreateExpedientGetTitleAndCenterUseCaseImplTest {

    @Mock
    private EventRepositoryClient eventRepositoryClient;
    @Mock
    private OperationRepositoryClient operationRepositoryClient;
    @Mock
    private HeadlineOperationRepositoryClient headlineOperationRepositoryClient;
    @Mock
    private AcumClientRepositoryClient acumClientRepositoryClient;
    @Mock
    private AcumOperationRepositoryClient acumOperationRepositoryClient;

    @InjectMocks
    private CreateExpedientGetTitleAndCenterUseCaseImpl useCase;

    private final String entity = "0049";
    private final Long originId = 123L;
    private final String productId = "PROD1";

    @BeforeEach
    void setUp() {
        // Event path setup
        Event event = Event.builder()
            .ownerName("Event Owner")
            .ownerDocument("12345678")
            .centerId("CENTER1")
            .owner("OWNER1")
            .build();
        when(eventRepositoryClient.findByEntityAndEvent(any(EventRequest.class)))
            .thenReturn(Mono.just(event));

        // Trade path setup - Normal product
        Operation operation = Operation.builder()
            .centerId("CENTER2")
            .owner("OWNER2")
            .build();
        HeadlineOperation headline = HeadlineOperation.builder()
            .name("Trade Owner")
            .document("87654321")
            .build();
        when(operationRepositoryClient.findByOperationIdAndEntityId(any(OperationRequest.class)))
            .thenReturn(Mono.just(operation));
        when(headlineOperationRepositoryClient.findByOperationIdAndEntityId(any(HeadlineOperationRequest.class)))
            .thenReturn(Mono.just(headline));

        // AC product path setup
        AcumClient acumClient = AcumClient.builder()
            .name("AC Owner")
            .document("11223344")
            .hostId("HOST1")
            .build();
        AcumOperation acumOperation = AcumOperation.builder()
            .centerId("CENTER3")
            .build();
        when(acumClientRepositoryClient.findByOperationId(originId))
            .thenReturn(Mono.just(acumClient));
        when(acumOperationRepositoryClient.findByOperationIdAndEntityId(any(AcumOperationRequest.class)))
            .thenReturn(Mono.just(acumOperation));
    }

    @Test
    void obtainTitleAndCenterData_eventPath() {
        StepVerifier.create(useCase.obtainTitleAndCenterData(entity, originId, "EVENT", productId))
            .expectNextMatches(tuple -> 
                tuple.getT1().equals("Event Owner") &&
                tuple.getT2().equals("12345678") &&
                tuple.getT3().equals("CENTER1") &&
                tuple.getT4().equals("OWNER1"))
            .verifyComplete();

        verify(eventRepositoryClient).findByEntityAndEvent(any(EventRequest.class));
    }

    @Test
    void obtainTitleAndCenterData_tradePath_normalProduct() {
        StepVerifier.create(useCase.obtainTitleAndCenterData(entity, originId, "TRADE", productId))
            .expectNextMatches(tuple -> 
                tuple.getT1().equals("Trade Owner") &&
                tuple.getT2().equals("87654321") &&
                tuple.getT3().equals("CENTER2") &&
                tuple.getT4().equals("OWNER2"))
            .verifyComplete();

        verify(operationRepositoryClient).findByOperationIdAndEntityId(any(OperationRequest.class));
        verify(headlineOperationRepositoryClient).findByOperationIdAndEntityId(any(HeadlineOperationRequest.class));
    }

    @Test
    void obtainTitleAndCenterData_tradePath_acProduct() {
        StepVerifier.create(useCase.obtainTitleAndCenterData(entity, originId, "TRADE", "AC"))
            .expectNextMatches(tuple -> 
                tuple.getT1().equals("AC Owner") &&
                tuple.getT2().equals("11223344") &&
                tuple.getT3().equals("CENTER3") &&
                tuple.getT4().equals("HOST1"))
            .verifyComplete();

        verify(acumClientRepositoryClient).findByOperationId(originId);
        verify(acumOperationRepositoryClient).findByOperationIdAndEntityId(any(AcumOperationRequest.class));
    }

    @Test
    void obtainTitleAndCenterData_eventPath_notFound() {
        when(eventRepositoryClient.findByEntityAndEvent(any(EventRequest.class)))
            .thenReturn(Mono.empty());

        StepVerifier.create(useCase.obtainTitleAndCenterData(entity, originId, "EVENT", productId))
            .expectError(RuntimeException.class)
            .verify();
    }
}
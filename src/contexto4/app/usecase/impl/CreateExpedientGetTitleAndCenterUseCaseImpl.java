package com.acelera.fx.digitalsignature.application.usecase.impl;

import com.acelera.broker.fx.db.domain.dto.*;
import com.acelera.broker.fx.db.domain.port.EventRepositoryClient;
import com.acelera.broker.fx.db.domain.port.HeadlineOperationRepositoryClient;
import com.acelera.broker.fx.db.domain.port.OperationRepositoryClient;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientGetTitleAndCenterUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateExpedientGetTitleAndCenterUseCaseImpl implements CreateExpedientGetTitleAndCenterUseCase {
    private final EventRepositoryClient eventRepositoryClient;
    private final OperationRepositoryClient operationRepositoryClient;
    private final HeadlineOperationRepositoryClient headlineOperationRepositoryClient;

    @Override
    public Mono<Tuple4<String, String, String, String>> obtainTitleAndCenterData(String entity, Long originId,
            String origin) {
        log.info("5. Obtener Datos titular y Centro");
        if ("EVENT".equals(origin)) {
            log.info("5.1 Buscando los datos en la BBDD, esquema ACELER");
            EventRequest eventRequest = EventRequest.builder().eventId(originId).entityId(entity).build();
            Mono<Event> eventMono = eventRepositoryClient.findByEntityAndEvent(eventRequest);
            return Mono.zip(
                    eventMono.map(Event::getOwnerName), // NOMBRE_TITULAR
                    eventMono.map(Event::getOwnerDocument), //DOCUMENTO_TITULAR
                    eventMono.map(Event::getCenterId), // IDCENT
                    eventMono.map(Event::getOwner)
            );
        } else {
            log.info("5.1 Buscando los datos en la BBDD, esquema ACELER - ACE_OPERACION y ACE_OPERACION_TITULARES");
            // TRADE: obtener desde ACE_OPERACION y ACE_OPERACION_TITULARES - Caso Si productId != AC
            Mono<Operation> operationMono = operationRepositoryClient.findByOperationIdAndEntityId(
                    OperationRequest.builder().operationId(originId).entityId(entity).build());
            Mono<HeadlineOperation> headlineMono = headlineOperationRepositoryClient.findByOperationIdAndEntityId(
                    HeadlineOperationRequest.builder().operationId(originId).entityId(entity).build());
            return Mono.zip(
                    headlineMono.map(HeadlineOperation::getName),
                    headlineMono.map(HeadlineOperation::getDocument),
                    operationMono.map(Operation::getCenterId),
                    operationMono.map(Operation::getOwner)
            );
        }
    }
}

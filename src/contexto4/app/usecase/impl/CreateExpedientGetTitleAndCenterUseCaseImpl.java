package com.acelera.fx.digitalsignature.application.usecase.impl;

import com.acelera.broker.fx.db.domain.dto.*;
import com.acelera.broker.fx.db.domain.port.*;
import com.acelera.fx.digitalsignature.application.usecase.port.CreateExpedientGetTitleAndCenterUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple4;

import static com.acelera.fx.digitalsignature.infrastructure.util.TradeSignatureConstants.ORIGIN_EVENT;

@Slf4j
@Component
@RequiredArgsConstructor
public class CreateExpedientGetTitleAndCenterUseCaseImpl implements CreateExpedientGetTitleAndCenterUseCase {
    private final EventRepositoryClient eventRepositoryClient;
    private final OperationRepositoryClient operationRepositoryClient;
    private final HeadlineOperationRepositoryClient headlineOperationRepositoryClient;
    private final AcumClientRepositoryClient acumClientRepositoryClient;
    private final AcumOperationRepositoryClient acumOperationRepositoryClient;

    @Override
    public Mono<Tuple4<String, String, String, String>> obtainTitleAndCenterData(String entity, Long originId,
            String origin, String productId) {
        log.info("5. Obtener Datos titular y Centro");
        if (ORIGIN_EVENT.equals(origin)) {
            log.info("5.1 Buscando los datos en la BBDD, esquema ACELER - Tabla ACE_EVENTO");
            EventRequest eventRequest = EventRequest.builder().eventId(originId).entityId(entity).build();
            Mono<Event> eventMono = eventRepositoryClient.findByEntityAndEvent(eventRequest)
                    .switchIfEmpty(Mono.error(new RuntimeException("Evento no encontrado")));
            return Mono.zip(
                    eventMono.map(Event::getOwnerName), // NOMBRE_TITULAR
                    eventMono.map(Event::getOwnerDocument), //DOCUMENTO_TITULAR
                    eventMono.map(Event::getCenterId), // IDCENT
                    eventMono.map(Event::getOwner) // TITULAR
            );
        } else {
            if(productId.equals("AC")){
                log.info("5.1 Buscando los datos en la BBDD, esquema ACELER - tabla ACE_ACUM_CLIENTE y ACE_ACUM_OPERACION");
                Mono<AcumClient> acumClient = acumClientRepositoryClient.findByOperationId(originId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Cliente no encontrado")));
                Mono<AcumOperation> acumOperationMono = acumOperationRepositoryClient.findByOperationIdAndEntityId(
                        AcumOperationRequest.builder().operationId(originId).entityId(entity).build());
                return Mono.zip(
                        acumClient.map(AcumClient::getName), // NOMBRE_TITULAR
                        acumClient.map(AcumClient::getDocument), //DOCUMENTO_TITULAR
                        acumOperationMono.map(AcumOperation::getCenterId), // IDCENT
                        acumClient.map(AcumClient::getHostId) // TITULAR
                );
            }
            log.info("5.1 Buscando los datos en la BBDD, esquema ACELER - tabla ACE_OPERACION y ACE_OPERACION_TITULARES");
            // TRADE: obtener desde ACE_OPERACION y ACE_OPERACION_TITULARES - Caso Si productId != AC
            Mono<Operation> operationMono = operationRepositoryClient.findByOperationIdAndEntityId(
                    OperationRequest.builder().operationId(originId).entityId(entity).build())
                    .switchIfEmpty(Mono.error(new RuntimeException("Operación no encontrada")));
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

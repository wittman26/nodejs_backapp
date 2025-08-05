package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.*;
import com.acelera.broker.fx.db.domain.port.*;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.broker.rest.dfd.domain.RestDfdClient;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.error.CustomErrorException;
import com.acelera.fx.digitalsignature.application.mapper.TradeSignatureMapper;
import com.acelera.fx.digitalsignature.application.mapper.TradeSignatureRequestMapper;
import com.acelera.fx.digitalsignature.domain.helper.TradeSignerHelper;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.GetTradeSignatureParameterDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignatureDto;
import com.acelera.fx.digitalsignature.domain.port.dto.TradeSignerDto;
import com.acelera.fx.digitalsignature.domain.port.service.ProductDocumentsService;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceGet;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureServiceSave;
import com.acelera.fx.digitalsignature.infrastructure.Constants;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateExpedientRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.CreateExpedientResponse;

import infrastructure.logica.TradeSignature;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.w3c.dom.events.Event;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.rmi.server.Operation;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureServiceSaveImpl implements TradeSignatureServiceSave {

    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;
    private final ProductDocumentsService productDocumentsService;
    private final DocumentSignatureRepositoryClient documentSignatureRepositoryClient;
    private final EventRepositoryClient eventRepositoryClient;
    private final OperationRepositoryClient operationRepositoryClient;
    private final HeadlineOperationRepositoryClient headlineOperationRepositoryClient;
    private final EventDisclaimerRepositoryClient eventDisclaimerRepositoryClient;
    private final OperationDisclaimerRepositoryClient operationDisclaimerRepositoryClient;
    private final TradeSignatureServiceGet tradeSignatureServiceGet;
    private final RestDfdClient restDfdClient;
    private final TradeSignerHelper tradeSignerHelper;

    @Override
    public Mono<TradeSignature> createOrUpdateSignature(Locale locale, String entity, TradeSignatureDto dto) {
        // Si es actualización, buscar y actualizar; si no, crear nueva
        return tradeSignatureRepositoryClient.find(TradeSignatureRequestMapper.INSTANCE.fromDtoToTradeSignatureFindRequest(dto, entity))
                .flatMap(tradeSignatureFound -> upsertTradeSignature(tradeSignatureFound, dto, entity)) // Logica de actualizar
                .switchIfEmpty(Mono.defer(() -> upsertTradeSignature(null, dto, entity))); // Logica de salvar
    }

    @Override
    public Mono<CreateExpedientResponse> createSignatureExpedient(Locale locale, String entity, Long originId, CreateExpedientRequest request) {
        String origin = tradeSignerHelper.isEventProduct(request.getProductId()) ? "EVENT" : "TRADE";

        return obtenerTradeSignature(entity, originId, request)
                .flatMap(tradeSignature -> obtenerFirmantes(locale, entity, tradeSignature, originId, origin)
                        .flatMap(signers -> obtenerTiposDeDocumento(entity, locale, request.getProductId())
                                .map(documentTypes -> construirRespuesta(tradeSignature, signers, documentTypes))
                        )
                );
    }

    private Mono<TradeSignature> obtenerTradeSignature(String entity, Long originId, CreateExpedientRequest request) {
        return tradeSignatureServiceGet.getTradeSignature(entity, originId, request)
                .doOnNext(ts -> log.info("1: TradeSignatureId obtenido: {}", ts.getTradeSignatureId()))
                .switchIfEmpty(Mono.error(new RuntimeException("TradeSignature no encontrado")));
    }

    private Mono<List<TradeSignerDto>> obtenerFirmantes(Locale locale, String entity, TradeSignature tradeSignature, Long originId, String origin) {
        var signerRequest = getSignerRequest(tradeSignature.getTradeSignatureId(), originId, origin);
        return tradeSignatureServiceGet.getTradeSignature(locale, entity, signerRequest)
                .map(GetTradeSignatureDto::getSigners)
                .doOnNext(signers -> {
                    log.info("2: Firmantes encontrados: {}", signers.size());
                    signers.forEach(signer -> log.info("SignerId: {}", signer.getSignerId()));
                });
    }
    private GetTradeSignatureParameterDto getSignerRequest(Long tradeSignatureId, Long originId, String origin) {
        return GetTradeSignatureParameterDto.builder()
                .tradeSignatureId(tradeSignatureId)
                .origin(origin)
                .originId(originId)
                .build();
    }    

    private Mono<List<ProductDocumentParameters>> obtenerTiposDeDocumento(String entity, Locale locale, String productId) {
        return productDocumentsService.findProductDocumentType(entity, locale, productId)
                .collectList()
                .doOnNext(documents -> {
                    log.info("3: Tipos de documentos por producto: {}", documents.size());
                    documents.forEach(doc -> log.info("DocumentType: {} - {}", doc.getProduct(), doc.getDocumentType()));
                });
    }

    private CreateExpedientResponse construirRespuesta(TradeSignature ts, List<TradeSignerDto> signers, List<ProductDocumentParameters> docs) {
        // TODO Contruiri lógica
        return CreateExpedientResponse.builder()
                .expedientId(123L)
                .build();
    }
    
    private Mono<CreateExpedientResponse> transformacion(List<ProductDocumentParameters> documentTypes, Locale locale, String entity, Long originId, CreateExpedientRequest request, String origin) {
        return obtenerDocumentSignatures(documentTypes, entity, originId, origin)
                .flatMap(documentSignatures -> validarCantidadDocumentos(documentSignatures, documentTypes)
                        .then(obtenerDatosTitularYCentro(entity, originId, origin))
                        .flatMap(tuple -> obtenerDisclaimer(entity, originId, request.getProductId())
                                .flatMap(disclaimer -> construirYCrearExpediente(tuple, disclaimer, documentSignatures, request, origin))
                        )
                );
    }

    private Mono<List<DocumentSignature>> obtenerDocumentSignatures(List<ProductDocumentParameters> documentTypes, String entity, Long originId, String origin) {
        List<Mono<DocumentSignature>> monos = documentTypes.stream()
                .map(docType -> {
                    DocumentRequest request = DocumentRequest.builder()
                            .entityId(entity)
                            .documentTypeId(docType.getDocumentType())
                            .operationId("TRADE".equals(origin) ? originId : null)
                            .eventId("EVENT".equals(origin) ? originId : null)
                            .build();
                    return "TRADE".equals(origin)
                            ? documentSignatureRepositoryClient.findByEntityAndOperationAndDocumentType(request)
                            : documentSignatureRepositoryClient.findByEntityAndEventAndDocumentType(request);
                })
                .toList();

        return Flux.mergeSequential(monos).collectList();
    }

    private Mono<Void> validarCantidadDocumentos(List<DocumentSignature> encontrados, List<ProductDocumentParameters> esperados) {
        if (encontrados.size() != esperados.size()) {
            return Mono.error(new RuntimeException("Algunos documentos no se encontraron."));
        }
        return Mono.empty();
    }

    private Mono<Tuple3<String, String, String>> obtenerDatosTitularYCentro(String entity, Long originId, String origin) {
        if ("EVENT".equals(origin)) {
            EventRequest eventRequest = EventRequest.builder().eventId(originId).entityId(entity).build();
            Mono<Event> eventMono = eventRepositoryClient.findByEntityAndEvent(eventRequest);
            return Mono.zip(
                    eventMono.map(Event::getOwnerName),
                    eventMono.map(Event::getOwnerDocument),
                    eventMono.map(Event::getCenterId)
            );
        } else {
            Mono<Operation> operationMono = operationRepositoryClient.findByOperationIdAndEntityId(
                    OperationRequest.builder().operationId(originId).entityId(entity).build());
            Mono<HeadlineOperation> headlineMono = headlineOperationRepositoryClient.findByOperationIdAndEntityId(
                    HeadlineOperationRequest.builder().operationId(originId).entityId(entity).build());
            return Mono.zip(
                    headlineMono.map(HeadlineOperation::getName),
                    headlineMono.map(HeadlineOperation::getDocument),
                    operationMono.map(Operation::getCenterId)
            );
        }
    }

    private Mono<String> obtenerDisclaimer(String entity, Long originId, String productId) {
        if (tradeSignerHelper.isEventProduct(productId)) {
            return eventDisclaimerRepositoryClient.findByEntityAndEventId(
                    EventDisclaimerRequest.builder().entity(entity).eventId(originId).build()
            ).map(EventDisclaimer::getContent);
        } else {
            return operationDisclaimerRepositoryClient.findByEntityAndTradeId(
                    OperationDisclaimerRequest.builder().entity(entity).tradeId(originId).build()
            ).map(OperationDisclaimer::getContent);
        }
    }

    private Mono<CreateExpedientResponse> construirYCrearExpediente(Tuple3<String, String, String> datos, String disclaimer, List<DocumentSignature> docs, CreateExpedientRequest request, String origin) {
        String ownerName = datos.getT1();
        String ownerDocument = datos.getT2();
        String center = datos.getT3();

        List<ExpedientRequest.Document> documentos = docs.stream()
                .map(doc -> ExpedientRequest.Document.builder()
                        .typeDoc("DOCUMENT")
                        .indPreContractual(true)
                        .build())
                .toList();

        ExpedientRequest expedientRequest = ExpedientRequest.builder()
                .sourceApp(ExpedientRequest.SourceApp.builder()
                        .code(Constants.ACELERA)
                        .operCode("EVENT".equals(origin) ? "ACEV" : "ACE" + origin)
                        .url("/v1/trades-signatures/expedients/{id}?status=")
                        .build())
                .startDate(ZonedDateTime.now())
                .endDate(ZonedDateTime.now().plusDays(5))
                .centre(center)
                .typeReference(Constants.DERIVADO_DIV)
                .customerId(ownerDocument)
                .productDesc(Constants.DERIVADO_DIV)
                .descExp(Constants.CONT_DER_DIV)
                .typeBox(Constants.B092)
                .catBox(Constants.DIVISAS)
                .channel(Constants.CHAN_OFI)
                .docs(documentos)
                .build();

        return restDfdClient.createExpedient(expedientRequest)
                .switchIfEmpty(Mono.error(new RuntimeException("DFD no devolvió expedientId")))
                .map(id -> CreateExpedientResponse.builder().expedientId(id).build());
    }

    public Mono<TradeSignature> upsertTradeSignature(TradeSignature tradeSignatureFound, TradeSignatureDto dto, String entity) {
        // Lógica de actualización/creación
        TradeSignature tradeSignature = TradeSignatureMapper.INSTANCE.fromDtoToTradeSignature(dto, entity);

        // Si es actualización, conserva el ID, validatedBo, OriginID y limpia la lista anterior
        if (tradeSignatureFound != null) {
            if (tradeSignatureFound.getExpedientId() != null) {
                return Mono.error(CustomErrorException.ofArguments(BAD_REQUEST, "error.fx.tradesignature.expedient.exists"));
            }
            tradeSignature.setValidatedBo(tradeSignatureFound.getValidatedBo());
            tradeSignature.setOriginId(tradeSignatureFound.getOriginId());
            tradeSignature.setTradeSignatureId(tradeSignatureFound.getTradeSignatureId());
        } else {
            if (tradeSignature.getOriginId() == null) {
                return Mono.error(CustomErrorException.ofArguments(NOT_FOUND, "error.fx.tradesignature.id.notFound"));
            }
        }

        tradeSignature.setTradeSignerList(tradeSignature.getTradeSignerList());

        // Asegura que cada signer tenga el tradeSignatureId correcto
        tradeSignature.getTradeSignerList()
                .forEach(signer -> signer.setTradeSignatureId(tradeSignature.getTradeSignatureId()));

        return tradeSignatureRepositoryClient.save(tradeSignature);
    }
}

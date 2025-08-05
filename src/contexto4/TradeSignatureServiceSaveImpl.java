package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.*;
import com.acelera.broker.fx.db.domain.port.*;
import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.broker.rest.dfd.domain.RestDfdClient;
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

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

        //  Obtener los nombres de los documentos desde ACE_DOCUMENTOS o ACE_EVENT_DOC
        List<Mono<DocumentSignature>> documentSignaturesMonos = documentTypes.stream()
                .map(docType -> {
                    DocumentRequest docRequest = DocumentRequest.builder()
                            .entityId(entity)
                            .documentTypeId(docType.getDocumentType())
                            .operationId("TRADE".equals(origin) ? originId : null)
                            .eventId("EVENT".equals(origin) ? originId : null)
                            .build();

                    return "TRADE".equals(origin)
                            ? documentSignatureRepositoryClient.findByEntityAndOperationAndDocumentType(docRequest)
                            : documentSignatureRepositoryClient.findByEntityAndEventAndDocumentType(docRequest);
                })
                .toList();

        return Flux.mergeSequential(documentSignaturesMonos)
                .collectList()
                .flatMap(documentSignatures -> {
                    if (documentSignatures.size() != documentTypes.size()) {
                        return Mono.error(new RuntimeException("Some document names could not be found."));
                    }

                    //  Obtener titular y centro
                    Mono<String> ownerNameMono;
                    Mono<String> ownerDocumentMono;
                    Mono<String> centerMono;

                    if ("EVENT".equals(origin)) {
                        EventRequest eventRequest = EventRequest.builder()
                                .eventId(originId)
                                .entityId(entity)
                                .build();

                        Mono<Event> eventMono = eventRepositoryClient.findByEntityAndEvent(eventRequest);

                        ownerNameMono = eventMono.map(Event::getOwnerName);
                        ownerDocumentMono = eventMono.map(Event::getOwnerDocument);
                        centerMono = eventMono.map(Event::getCenterId);

                    } else {
                        // TRADE: obtener desde ACE_OPERACION y ACE_OPERACION_TITULARES
                        Mono<Operation> operationMono = operationRepositoryClient.findByOperationIdAndEntityId(
                                OperationRequest.builder()
                                        .operationId(originId)
                                        .entityId(entity)
                                        .build());

                        Mono<HeadlineOperation> headlineMono = headlineOperationRepositoryClient.findByOperationIdAndEntityId(
                                HeadlineOperationRequest.builder()
                                        .operationId(originId)
                                        .entityId(entity)
                                        .build());

                        ownerNameMono = headlineMono.map(HeadlineOperation::getName);
                        ownerDocumentMono = headlineMono.map(HeadlineOperation::getDocument);
                        centerMono = operationMono.map(Operation::getCenterId);
                    }

                    // Combinar los resultados
                    return Mono.zip(ownerNameMono, ownerDocumentMono, centerMono)
                            .flatMap(tuple -> {
                                String ownerName = tuple.getT1();
                                String ownerDocument = tuple.getT2();
                                String center = tuple.getT3();

                                String productId = request.getProductId();
                                Mono<String> disclaimerContentMono;

                                if (tradeSignerHelper.isEventProduct(productId)) {
                                    disclaimerContentMono = eventDisclaimerRepositoryClient.findByEntityAndEventId(
                                            EventDisclaimerRequest.builder()
                                                    .entity(entity)
                                                    .eventId(originId)
                                                    .build()
                                    ).map(EventDisclaimer::getContent);
                                } else {
                                    disclaimerContentMono = operationDisclaimerRepositoryClient.findByEntityAndTradeId(
                                            OperationDisclaimerRequest.builder()
                                                    .entity(entity)
                                                    .tradeId(originId)
                                                    .build()
                                    ).map(OperationDisclaimer::getContent);
                                }

                                return disclaimerContentMono.flatMap(disclaimerContent -> {

                                    List<ExpedientRequest.Document> documents = documentSignatures.stream()
                                            .map(doc -> ExpedientRequest.Document.builder()
                                                    .typeDoc("DOCUMENT")
                                                    .indPreContractual(true)
                                                    .build()
                                            ).toList();

                                    ExpedientRequest expedientRequest = ExpedientRequest.builder()
                                            .sourceApp(ExpedientRequest.SourceApp.builder()
                                                    .code(Constants.ACELERA)
                                                    .operCode(tradeSignerHelper.isEventProduct(request.getProductId()) ? "ACEV" : "ACE" + origin)
                                                    .url("/v1/trades-signatures/expedients/{id}?status=" )
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
                                            .docs(documents)
                                            .build();

                                    return restDfdClient.createExpedient(expedientRequest)
                                            .flatMap(expedientIdResponse -> {
                                                if ( expedientIdResponse == null) {
                                                    return Mono.error(new RuntimeException("DFD response did not return expedientId"));
                                                }
                                                return Mono.just(
                                                        CreateExpedientResponse.builder()
                                                                .expedientId(expedientIdResponse)
                                                                .build()
                                                );
                                            });
                                });
                            });
                });
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

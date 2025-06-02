package com.acelera.fx.digitalsignature.infrastructure.response;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignatureResponse {
    private Integer tradeSignatureId;
}

package com.acelera.broker.fx.db.domain.dto;

import com.acelera.dto.AuditZonedFields;
import lombok.*;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSigner extends AuditZonedFields {

    private Long tradeSignerId;
    private Long tradeSignatureId;
    private String documentType;
    private String documentNumber;
    private String signerId;
    private String name;
    private String isClient;
    private String interventionType;
}

package com.acelera.broker.fx.db.domain.dto;

import com.acelera.dto.AuditZonedFields;
import lombok.*;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignature extends AuditZonedFields {

    private Long tradeSignatureId;
    private String entity;
    private Long originId;
    private String origin;
    private String productId;
    private String signatureType;
    private String indicatorSSCC;
    private String validatedBo;
    private Long expedientId;
    private List<TradeSigner> tradeSignerList;
}

package com.acelera.broker.fx.db.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignatureFindRequest {
    private Long tradeSignatureId;
    private String entity;
    private Long originId;
    private String productId;
}


package com.acelera.fx.digitalsignature.infrastructure.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignatureRequest {
    @Schema(name = "tradeSignatureId", example = "9876", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long tradeSignatureId;

    @Schema(name = "originId", example = "123450", description = "Unique identifier for trade/event", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long originId;

    @Schema(name = "productId", description = "Unique identifier of product", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String productId;

    @Schema(name = "signatureType", example = "DIGITAL", description = "Indicator of the signature type. PAPER / DIGITAL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String signatureType;

    @Schema(name = "indicatorSSCC", example = "false", description = "Indicator if the product is sent to Partenon SSCC, only applicable for derivatives", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean indicatorSSCC;

    @Schema(name = "signers", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<TradeSignerRequest> signers;
}


package com.acelera.fx.digitalsignature.infrastructure.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TradeSignerRequest {
    @Schema(name = "signerId", example = "F000000583", description = "Person Signer Id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String signerId;

    @Schema(name = "isClient", example = "true", description = "Indicates wether the signer is a client or not", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isClient;

    @Schema(name = "document", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SignerDocument document;

    @Schema(name = "name", example = "RMV VALORES OLC SAN CLIENTE NO SPB", description = "Signer full name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    @Schema(name = "interventionType", example = "01", description = "Signer intervention type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String interventionType;
}


package com.acelera.fx.digitalsignature.application.service.mapper;

import com.acelera.broker.fx.db.domain.dto.TradeSigner;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignerRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TradeSignerMapper {
    TradeSignerMapper INSTANCE = Mappers.getMapper(TradeSignerMapper.class);

    @Mapping(target = "documentNumber", source = "document.number")
    @Mapping(target = "documentType", source = "document.type")
    @Mapping(target = "isClient", source = "isClient", qualifiedByName = "booleanToString")
    TradeSigner toTradeSigner(TradeSignerRequest tradeSignerRequest);

    @Named("booleanToString")
    static String mapBooleanToString(Boolean isClient) {
        return Boolean.TRUE.equals(isClient) ? "Y":"N";
    }

}

package com.acelera.fx.digitalsignature.application.service.mapper;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TradeSignatureMapper {
    TradeSignatureMapper INSTANCE = Mappers.getMapper(TradeSignatureMapper.class);

    @Mapping(target = "indicatorSSCC", source = "indicatorSSCC", qualifiedByName = "booleanToString")
    @Mapping(target = "tradeSignerList", source = "signers")
    TradeSignature toTradeSignature(TradeSignatureRequest tradeSignatureRequest);

    @Named("booleanToString")
    static String mapBooleanToString(Boolean indicatorSSCC) {
        return Boolean.TRUE.equals(indicatorSSCC) ? "Y":"N";
    }
}



package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSignatureFindRequest;
import com.acelera.broker.fx.db.domain.dto.TradeSigner;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignatureMapper;
import com.acelera.fx.digitalsignature.application.service.mapper.TradeSignerMapper;
import com.acelera.fx.digitalsignature.domain.port.service.TradeSignatureService;
import com.acelera.fx.digitalsignature.infrastructure.request.CreateDocumentRequest;
import com.acelera.fx.digitalsignature.infrastructure.request.TradeSignatureRequest;
import com.acelera.fx.digitalsignature.infrastructure.response.CreateDocumentResponse;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignatureResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;

@Slf4j
@Service
@RequiredArgsConstructor
public class TradeSignatureServiceImpl implements TradeSignatureService {

    private final TradeSignatureRepositoryClient tradeSignatureRepositoryClient;

    private static final String ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE = "Se espera incluir originId o transferId pero no ambos";

    @Override
    public Mono<CreateDocumentResponse> createDocument(String originId, Locale locale, String entity, CreateDocumentRequest request) {
        return Mono.empty();
    }

    /**
     * Crea o actualiza una firma de operación.
     * @param locale el locale
     * @param entity la entidad
     * @param request la petición de firma
     * @return Mono con la respuesta de la firma
     */
    @Override
    public Mono<TradeSignatureResponse> createOrUpdateSignature(Locale locale, String entity, TradeSignatureRequest request) {
        boolean hasTradeSignatureId = request.getTradeSignatureId() != null;
        boolean hasOriginId = request.getOriginId() != null;

        validateCreateOrUpdateParams(hasTradeSignatureId, hasOriginId);

        if (hasTradeSignatureId) {
            // Lógica de Actualización
            return findTradeSignature(request, entity)
                    .flatMap(tradeSignatureFound -> updateTradeSignature(tradeSignatureFound, request, entity))
                    .switchIfEmpty(Mono.error(() -> new NoSuchElementException("TradeSignature not found")));
        } else {
            // Consultar si existe
            return findTradeSignature(request, entity)
                    .flatMap(tradeSignatureFound -> updateTradeSignature(tradeSignatureFound, request, entity))// Lógica de Actualización
                    .switchIfEmpty(saveTradeSignature(entity, request)); // Lógica de Alta
        }
    }

    /**
     * Valida los parámetros de entrada para crear o actualizar una firma.
     */
    private void validateCreateOrUpdateParams(boolean hasTradeSignatureId, boolean hasOriginId) {
        if (hasTradeSignatureId == hasOriginId) {
            log.error(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
            throw new IllegalArgumentException(ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE);
        }
    }

    private Mono<TradeSignatureResponse> saveTradeSignature(String entity, TradeSignatureRequest request) {
        // Prepara DTO para guardar padre e hijos
        TradeSignature tradeSignature = TradeSignatureMapper.INSTANCE.toTradeSignature(request);
        tradeSignature.setEntity(entity);
        tradeSignature.setValidatedBo("PENDING");
        tradeSignature.setOrigin(isEventProduct(tradeSignature.getProductId()) ? "EVENT" : "TRADE");
        tradeSignature.setTradeSignerList(
                request.getSigners().stream()
                        .map(TradeSignerMapper.INSTANCE::toTradeSigner)
                        .toList()
        );

        return tradeSignatureRepositoryClient.save(tradeSignature)
                .flatMap(response -> createResponse(response.getTradeSignatureId()));
    }

    private Mono<TradeSignature> findTradeSignature(TradeSignatureRequest request,
            String entity) {

        // Prepara los filtros
        TradeSignatureFindRequest filters = TradeSignatureFindRequest.builder()
                .tradeSignatureId(request.getTradeSignatureId())
                .entity(entity)
                .originId(request.getOriginId())
                .productId(request.getProductId())
                .build();

        return tradeSignatureRepositoryClient.find(filters);
    }

    private Mono<TradeSignatureResponse> updateTradeSignature(TradeSignature tradeSignatureFound, TradeSignatureRequest request,
            String entity) {

        // Mapear los hijos del request a DTO
        List<TradeSigner> incomingSigners = request.getSigners().stream()
                .map(TradeSignerMapper.INSTANCE::toTradeSigner)
                .toList();

        // Sincronizar la lista de hijos
        tradeSignatureFound.getTradeSignerList().clear();

        for (TradeSigner signer : incomingSigners) {
            signer.setTradeSignatureId(tradeSignatureFound.getTradeSignatureId()); // Asocia el hijo al padre
            tradeSignatureFound.getTradeSignerList().add(signer);
        }
        var requestUpdate = TradeSignatureMapper.INSTANCE.toTradeSignature(request);

        // Actualiza otros campos de la cabecera si es necesario
        tradeSignatureFound.setProductId(requestUpdate.getProductId());
        tradeSignatureFound.setSignatureType(requestUpdate.getSignatureType());
        tradeSignatureFound.setIndicatorSSCC(requestUpdate.getIndicatorSSCC());


        return tradeSignatureRepositoryClient.save(tradeSignatureFound)
                .flatMap(response -> createResponse(response.getTradeSignatureId()));
    }

    private Mono<TradeSignatureResponse> createResponse(Long tradeSignatureId) {
        return Mono.just(TradeSignatureResponse.builder().tradeSignatureId(
                tradeSignatureId.intValue()).build());
    }

    private boolean isEventProduct(String productId) {
        return Arrays.asList("AN", "IN", "PC", "PS").contains(productId);
    }

}

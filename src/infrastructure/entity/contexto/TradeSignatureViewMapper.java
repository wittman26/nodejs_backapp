package com.acelera.fx.digitalsignature.application.service.mapper;

import com.acelera.broker.fx.db.domain.dto.TradeSignatureExpedientView;
import com.acelera.fx.digitalsignature.infrastructure.response.GetTradeSignatureResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.Arrays;

@Mapper
public interface TradeSignatureViewMapper {
    TradeSignatureViewMapper INSTANCE = Mappers.getMapper(TradeSignatureViewMapper.class);

    @Mapping(target = "origin", source = "productId", qualifiedByName = "obtainOrigin")
    @Mapping(target = "expedient", expression = "java(mapExpedientInfo(tradeSignatureExpedientView))")
    GetTradeSignatureResponse toGetTradeSignatureResponse(TradeSignatureExpedientView tradeSignatureExpedientView);

    @Named("obtainOrigin")
    static String obtainOrigin(String productId) {
        return Arrays.asList("AN", "IN", "PC", "PS").contains(productId) ? "EVENT" : "TRADE";
    }

    // Lógica para mapear ExpedientInfo
    static ExpedientInfo mapExpedientInfo(TradeSignatureExpedientView view) {
        if (view.getExpedientId() == null) return null;

        // Formato de fecha y conversión a local
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        java.time.ZoneId localZone = java.time.ZoneId.systemDefault();

        String startDate = view.getStartDate() != null
                ? view.getStartDate().atZone(java.time.ZoneOffset.UTC).withZoneSameInstant(localZone).format(formatter)
                : null;
        String endDate = view.getEndDate() != null
                ? view.getEndDate().atZone(java.time.ZoneOffset.UTC).withZoneSameInstant(localZone).format(formatter)
                : null;

        // isActive
        boolean isActive = view.getEndDate() != null && view.getEndDate().isAfter(java.time.LocalDateTime.now());

        // statusDescription
        String statusDescription = null;
        if ("PENDING".equalsIgnoreCase(view.getExpedientStatus())) statusDescription = "Pte. firma";
        else if ("COMPLETED".equalsIgnoreCase(view.getExpedientStatus())) statusDescription = "Firmada";
        else if ("CANCELLED".equalsIgnoreCase(view.getExpedientStatus())) statusDescription = "Cancelada";

        // hasClauses
        Boolean hasClauses = "Y".equalsIgnoreCase(view.getHasClauses());

        return ExpedientInfo.builder()
                .expedientId(view.getExpedientId())
                .isActive(isActive)
                .startDate(startDate)
                .endDate(endDate)
                .status(view.getExpedientStatus())
                .statusDescription(statusDescription)
                .hasClauses(hasClauses)
                .build();
    }
}

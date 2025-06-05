package com.acelera.fx.digitalsignature.application.service.mapper;

import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import com.acelera.fx.digitalsignature.infrastructure.response.TradeSignersResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TradeSignerDocumentStatusViewMapper {
    TradeSignerDocumentStatusViewMapper INSTANCE = Mappers.getMapper(TradeSignerDocumentStatusViewMapper.class);

    @Mapping(target = "documentNumber", source = "document.number")
    @Mapping(target = "documentType", source = "document.type")
    @Mapping(target = "isClient", source = "isClient", qualifiedByName = "booleanToString")
    TradeSignersResponse toTradeSignersResponse(TradeSignerDocumentStatusView tradeSignerDocumentStatusView);

    @Named("booleanToString")
    static String mapBooleanToString(Boolean isClient) {
        return Boolean.TRUE.equals(isClient) ? "Y":"N";
    }

}

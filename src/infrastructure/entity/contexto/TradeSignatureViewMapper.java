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
    GetTradeSignatureResponse toGetTradeSignatureResponse(TradeSignatureExpedientView tradeSignatureExpedientView);

    @Named("obtainOrigin")
    static String obtainOrigin(String productId) {
        return Arrays.asList("AN", "IN", "PC", "PS").contains(productId) ? "EVENT" : "TRADE";
    }

}

package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.mapper;

import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.dto.TradeSigner;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.TradeSignatureModel;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.TradeSignerModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;
import java.time.ZonedDateTime;
import java.time.LocalDateTime;
import java.util.List;

@Mapper(componentModel = "spring", uses = {TradeSignerMapper.class})
public interface TradeSignatureMapper {
    TradeSignatureMapper INSTANCE = Mappers.getMapper(TradeSignatureMapper.class);

    @Mappings({
        @Mapping(target = "tradeSignatureId", source = "tradeSignatureId"),
        @Mapping(target = "entity", source = "entity"),
        @Mapping(target = "originId", source = "originId"),
        @Mapping(target = "origin", source = "origin"),
        @Mapping(target = "productId", source = "productId"),
        @Mapping(target = "signatureType", source = "signatureType"),
        @Mapping(target = "indicatorSscc", source = "indicatorSSCC"),
        @Mapping(target = "validatedBo", source = "validatedBo"),
        @Mapping(target = "expedientId", source = "expedientId"),
        @Mapping(target = "tradeSignerList", source = "tradeSignerList"),
        // Auditoría
        @Mapping(target = "usualta", source = "usualta"),
        @Mapping(target = "fecalta", expression = "java(auditZonedToLocal(tradeSignature.getFecalta()))"),
        @Mapping(target = "usumodi", source = "usumodi"),
        @Mapping(target = "fecmodi", expression = "java(auditZonedToLocal(tradeSignature.getFecmodi()))")
    })
    TradeSignatureModel fromDomain(TradeSignature tradeSignature);

    @Mappings({
        @Mapping(target = "tradeSignatureId", source = "tradeSignatureId"),
        @Mapping(target = "entity", source = "entity"),
        @Mapping(target = "originId", source = "originId"),
        @Mapping(target = "origin", source = "origin"),
        @Mapping(target = "productId", source = "productId"),
        @Mapping(target = "signatureType", source = "signatureType"),
        @Mapping(target = "indicatorSSCC", source = "indicatorSscc"),
        @Mapping(target = "validatedBo", source = "validatedBo"),
        @Mapping(target = "expedientId", source = "expedientId"),
        @Mapping(target = "tradeSignerList", source = "tradeSignerList"),
        // Auditoría
        @Mapping(target = "usualta", source = "usualta"),
        @Mapping(target = "fecalta", expression = "java(auditLocalToZoned(tradeSignatureModel.getFecalta()))"),
        @Mapping(target = "usumodi", source = "usumodi"),
        @Mapping(target = "fecmodi", expression = "java(auditLocalToZoned(tradeSignatureModel.getFecmodi()))")
    })
    TradeSignature toDomain(TradeSignatureModel tradeSignatureModel);

    // Métodos utilitarios para fechas
    static LocalDateTime auditZonedToLocal(ZonedDateTime zdt) {
        return zdt != null ? zdt.toLocalDateTime() : null;
    }
    static ZonedDateTime auditLocalToZoned(LocalDateTime ldt) {
        return ldt != null ? ldt.atZone(java.time.ZoneId.systemDefault()) : null;
    }
}

@Mapper(componentModel = "spring")
interface TradeSignerMapper {
    TradeSignerMapper INSTANCE = Mappers.getMapper(TradeSignerMapper.class);

    @Mappings({
        @Mapping(target = "tradeSignerId", source = "tradeSignerId"),
        @Mapping(target = "tradeSignatureId", source = "tradeSignatureId"),
        @Mapping(target = "documentType", source = "documentType"),
        @Mapping(target = "documentNumber", source = "documentNumber"),
        @Mapping(target = "signerId", source = "signerId"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "isClient", source = "isClient"),
        @Mapping(target = "interventionType", source = "interventionType"),
        // Auditoría
        @Mapping(target = "usualta", source = "usuAlta"),
        @Mapping(target = "fecalta", expression = "java(auditLocalToZoned(tradeSignerModel.getFecAlta()))"),
        @Mapping(target = "usumodi", source = "usuModi"),
        @Mapping(target = "fecmodi", expression = "java(auditLocalToZoned(tradeSignerModel.getFecModi()))")
    })
    TradeSigner toDomain(TradeSignerModel tradeSignerModel);

    @Mappings({
        @Mapping(target = "tradeSignerId", source = "tradeSignerId"),
        @Mapping(target = "tradeSignatureId", source = "tradeSignatureId"),
        @Mapping(target = "documentType", source = "documentType"),
        @Mapping(target = "documentNumber", source = "documentNumber"),
        @Mapping(target = "signerId", source = "signerId"),
        @Mapping(target = "name", source = "name"),
        @Mapping(target = "isClient", source = "isClient"),
        @Mapping(target = "interventionType", source = "interventionType"),
        // Auditoría
        @Mapping(target = "usuAlta", source = "usualta"),
        @Mapping(target = "fecAlta", expression = "java(auditZonedToLocal(tradeSigner.getFecalta()))"),
        @Mapping(target = "usuModi", source = "usumodi"),
        @Mapping(target = "fecModi", expression = "java(auditZonedToLocal(tradeSigner.getFecmodi()))")
    })
    TradeSignerModel fromDomain(TradeSigner tradeSigner);

    static LocalDateTime auditZonedToLocal(ZonedDateTime zdt) {
        return zdt != null ? zdt.toLocalDateTime() : null;
    }
    static ZonedDateTime auditLocalToZoned(LocalDateTime ldt) {
        return ldt != null ? ldt.atZone(java.time.ZoneId.systemDefault()) : null;
    }
}


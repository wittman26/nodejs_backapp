package com.acelera.fx.digitalsignature.infrastructure.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GetTradeSignatureResponse {

    @Schema(name = "tradeSignatureId", example = "9876", description = "Trade Signature Id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long tradeSignatureId;

    @Schema(name = "entity", example = "0049", description = "Entity Id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String entity;

    @Schema(name = "originId", example = "123450", description = "Unique identifier for trade/event", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Long originId;

    @Schema(name = "origin", example = "TRADE", description = "Identificator of TRADE or EVENT", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String origin;

    @Schema(name = "productId", description = "Unique identifier of product", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String productId;

    @Schema(name = "signatureType", example = "DIGITAL", description = "Indicator of the signature type. PAPER / DIGITAL", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String signatureType;

    @Schema(name = "indicatorSSCC", example = "false", description = "Indicator if the product is sent to Partenon SSCC, only applicable for derivatives", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean indicatorSSCC;

    @Schema(name = "validatedBO", example = "false", description = "Indicator if the signature completion has been validated in the BO systems", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean validatedBO;

    @Schema(name = "expedient", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private ExpedientInfo expedient;

    @Valid
    @Schema(name = "signers", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<@Valid TradeSignersResponse> signers = new ArrayList<>();

}

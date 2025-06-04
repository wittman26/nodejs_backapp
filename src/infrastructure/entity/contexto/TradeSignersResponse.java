package com.acelera.fx.digitalsignature.infrastructure.response;

import com.acelera.fx.digitalsignature.infrastructure.request.SignerDocument;
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
public class TradeSignersResponse {
    @Schema(name = "signerId", example = "F000000583", description = "Person Signer Id", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String signerId;

    @Schema(name = "isClient", example = "true", description = "Indicates wether the signer is a client or not", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isClient;

    @Schema(name = "signerColour", example = "YELLOW", description = "Indicates the signature global colour for the signer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String signerColour;

    @Schema(name = "document", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private SignerDocument document;

    @Schema(name = "name", example = "RMV VALORES OLC SAN CLIENTE NO SPB", description = "Signer full name", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String name;

    @Schema(name = "interventionType", example = "01", description = "Signer intervention type", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String interventionType;

    @Schema(name = "docs", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Valid
    private List<@Valid StatusDocumentPerSigner> docs = new ArrayList<>();
}

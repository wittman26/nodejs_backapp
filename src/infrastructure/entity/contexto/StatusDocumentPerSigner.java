package com.acelera.fx.digitalsignature.infrastructure.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatusDocumentPerSigner {
    @Schema(name = "gnId", example = "48e9a1fd954748a7936e5e79558d7a5b", description = "Document Filenet GN ID", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String gnId;

    @Schema(name = "documentalType", example = "COMVEN_FX_PREC", description = "Documental type of the document", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String documentalType;

    @Schema(name = "isPrecontractual", example = "true", description = "Indicator if the document is precontractual (true) or contractual (false)", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isPrecontractual;

    @Schema(name = "documentColour", example = "GREEN", description = "Indicates the signature colour of the document for the signer", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String documentColour;

    @Schema(name = "isSigned", example = "true", description = "Indicator if the signer has signed the document", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean isSigned;

    @Schema(name = "signatureDate", example = "2025-04-21T15:28:51Z", description = "Indicates the date and time when the document was signed", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime signatureDate;

    @Schema(name = "hasClauses", example = "false", description = "Indicator if the document has clauses", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private Boolean hasClauses;
}

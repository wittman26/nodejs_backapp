package com.acelera.fx.digitalsignature.domain.helper;

import com.acelera.broker.fx.db.domain.dto.TradeSignerDocumentStatusView;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TradeSignerHelper {
    public static final String ERROR_MESSAGE_DIGITAL_SIGNATURE_CREATE_UPDATE_SIGNATURE = "Se espera incluir originId o transferId pero no ambos";
    public static final String ERROR_MESSAGE_NO_TRANSFER_ID_FOUND = "No se ha encontrado el Transfer Id Proporcionado";
    public static final String ERROR_MESSAGE_DIGITAL_SIGNATURE_GET_SIGNATURE = "Se espera incluir originId y Origin si transferId no se proporciona";
    public static final String ORIGIN_EVENT = "EVENT";
    public static final String ORIGIN_TRADE = "TRADE";
    public static final String VALIDATED_BO_DEFAULT = "PENDING";

    public String getSignerColour(List<TradeSignerDocumentStatusView> signerDocs) {
        boolean allSigned = signerDocs.stream().allMatch(doc -> "Y".equals(doc.getSignedDoc()));
        boolean allNotSigned = signerDocs.stream().allMatch(doc -> "N".equals(doc.getSignedDoc()));
        if (allSigned) return "GREEN";
        if (allNotSigned) return "RED";
        return "YELLOW";
    }
}

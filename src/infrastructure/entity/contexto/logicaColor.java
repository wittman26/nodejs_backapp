public import java.util.*;
import java.util.stream.*;

public List<TradeSignersResponse> mapSignersWithColour(List<TradeSignerDocumentStatusView> views) {
    // Agrupa por signerId
    Map<String, List<TradeSignerDocumentStatusView>> grouped = views.stream()
        .collect(Collectors.groupingBy(TradeSignerDocumentStatusView::getSignerId));

    List<TradeSignersResponse> result = new ArrayList<>();

    for (Map.Entry<String, List<TradeSignerDocumentStatusView>> entry : grouped.entrySet()) {
        List<TradeSignerDocumentStatusView> signerDocs = entry.getValue();

        // Determina el colour
        boolean allSigned = signerDocs.stream().allMatch(doc -> "Y".equals(doc.getSignedDoc()));
        boolean allNotSigned = signerDocs.stream().allMatch(doc -> "N".equals(doc.getSignedDoc()));

        String signerColour;
        if (allSigned) {
            signerColour = "GREEN";
        } else if (allNotSigned) {
            signerColour = "RED";
        } else {
            signerColour = "YELLOW";
        }

        // Uso el primer documento como base para los datos del signer
        TradeSignerDocumentStatusView base = signerDocs.get(0);

        TradeSignersResponse response = TradeSignersResponse.builder()
            .signerId(base.getSignerId())
            .isClient("Y".equalsIgnoreCase(base.getIsClient()))
            .signerColour(signerColour)
            .name(base.getName())
            .interventionType(base.getInterventionType())
            // Mapeo los docs individuales 
            .docs(signerDocs.stream()
                .map(doc -> /* aquí uso mapper para StatusDocumentPerSigner */ null)
                .collect(Collectors.toList()))
            .build();

        result.add(response);
    }
    return result;
}


/*********************** */

import java.util.*;
import java.util.stream.*;

public List<TradeSignersResponse> mapSignersWithColour(List<TradeSignerDocumentStatusView> views) {
    return views.stream()
        .collect(Collectors.groupingBy(TradeSignerDocumentStatusView::getSignerId))
        .entrySet()
        .stream()
        .map(entry -> {
            List<TradeSignerDocumentStatusView> signerDocs = entry.getValue();
            TradeSignerDocumentStatusView base = signerDocs.get(0);

            String signerColour = getSignerColour(signerDocs);

            return TradeSignersResponse.builder()
                .signerId(base.getSignerId())
                .isClient("Y".equalsIgnoreCase(base.getIsClient()))
                .signerColour(signerColour)
                .name(base.getName())
                .interventionType(base.getInterventionType())
                .docs(signerDocs.stream()
                    .map(doc -> mapToStatusDocumentPerSigner(doc)) // <-- Usa tu mapper aquí
                    .collect(Collectors.toList()))
                .build();
        })
        .collect(Collectors.toList());
}

private String getSignerColour(List<TradeSignerDocumentStatusView> signerDocs) {
    boolean allSigned = signerDocs.stream().allMatch(doc -> "Y".equals(doc.getSignedDoc()));
    boolean allNotSigned = signerDocs.stream().allMatch(doc -> "N".equals(doc.getSignedDoc()));
    
    if (allSigned) return "GREEN";
    if (allNotSigned) return "RED";
    return "YELLOW";
}

// Simulación del mapper (debes reemplazarlo por tu implementación real)
private StatusDocumentPerSigner mapToStatusDocumentPerSigner(TradeSignerDocumentStatusView doc) {
    // TODO: Implementa tu lógica de mapeo real aquí
    return null;
}

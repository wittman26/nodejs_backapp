    public List<TradeSignersResponse> mapSignersWithColour(List<TradeSignerDocumentStatusView> views) {
        return views.stream()
                .collect(Collectors.groupingBy(TradeSignerDocumentStatusView::getSignerId))
                .entrySet()
                .stream()
                .map(entry -> {
                    List<TradeSignerDocumentStatusView> signerDocs = entry.getValue();
                    TradeSignerDocumentStatusView base = signerDocs.get(0);

                    String signerColour = getSignerColour(signerDocs);

                    var resultado = TradeSignerDocumentStatusViewMapper.INSTANCE.toTradeSignersResponse(base);
                    resultado.setSignerColour(signerColour);
                    resultado.setDocs(signerDocs.stream().map(this::mapearDoc).toList()); // <-- Uso mi mapper aquí
                    return resultado;
                }).toList();
    }

    private StatusDocumentPerSigner mapearDoc(TradeSignerDocumentStatusView doc) {
        // TODO
        return StatusDocumentPerSigner.builder().build();
    }

    private String getSignerColour(List<TradeSignerDocumentStatusView> signerDocs) {
        boolean allSigned = signerDocs.stream().allMatch(doc -> "Y".equals(doc.getSignedDoc()));
        boolean allNotSigned = signerDocs.stream().allMatch(doc -> "N".equals(doc.getSignedDoc()));

        if (allSigned) return "GREEN";
        if (allNotSigned) return "RED";
        return "YELLOW";
    }

    /*************** */

    // Tests

    import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class LogicaColorRefactorTest {

    private final logicaColorRefactor service = new logicaColorRefactor();

    private TradeSignerDocumentStatusView doc(String signerId, String signedDoc) {
        TradeSignerDocumentStatusView doc = new TradeSignerDocumentStatusView();
        doc.setSignerId(signerId);
        doc.setSignedDoc(signedDoc);
        return doc;
    }

    @Test
    void testAllSignedGreen() {
        List<TradeSignerDocumentStatusView> docs = List.of(
                doc("A", "Y"),
                doc("A", "Y")
        );
        var result = service.mapSignersWithColour(docs);
        assertEquals(1, result.size());
        assertEquals("GREEN", result.get(0).getSignerColour());
    }

    @Test
    void testAllNotSignedRed() {
        List<TradeSignerDocumentStatusView> docs = List.of(
                doc("B", "N"),
                doc("B", "N")
        );
        var result = service.mapSignersWithColour(docs);
        assertEquals(1, result.size());
        assertEquals("RED", result.get(0).getSignerColour());
    }

    @Test
    void testMixedYellow() {
        List<TradeSignerDocumentStatusView> docs = List.of(
                doc("C", "Y"),
                doc("C", "N"),
                doc("C", "Y")
        );
        var result = service.mapSignersWithColour(docs);
        assertEquals(1, result.size());
        assertEquals("YELLOW", result.get(0).getSignerColour());
    }

    @Test
    void testMultipleSigners() {
        List<TradeSignerDocumentStatusView> docs = List.of(
                doc("A", "Y"),
                doc("A", "Y"),
                doc("B", "N"),
                doc("B", "N"),
                doc("C", "Y"),
                doc("C", "N")
        );
        var result = service.mapSignersWithColour(docs);
        assertEquals(3, result.size());
        assertTrue(result.stream().anyMatch(r -> r.getSignerId().equals("A") && r.getSignerColour().equals("GREEN")));
        assertTrue(result.stream().anyMatch(r -> r.getSignerId().equals("B") && r.getSignerColour().equals("RED")));
        assertTrue(result.stream().anyMatch(r -> r.getSignerId().equals("C") && r.getSignerColour().equals("YELLOW")));
    }
}
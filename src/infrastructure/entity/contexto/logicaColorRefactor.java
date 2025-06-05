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
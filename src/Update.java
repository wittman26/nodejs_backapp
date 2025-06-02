public public void updateTradeSignature(Long id, TradeSignatureRequest request) {
    TradeSignature entity = repository.findById(id)
        .orElseThrow(() -> new NotFoundException("No existe"));

    // Mapear los hijos del request a entidades
    List<TradeSigner> incomingSigners = request.getSigners().stream()
        .map(dto -> TradeSignerMapper.INSTANCE.toEntity(dto))
        .collect(Collectors.toList());

    // Sincronizar la lista de hijos
    entity.getTradeSignerList().clear(); // Elimina los hijos actuales (orphanRemoval los borra en DB)
    for (TradeSigner signer : incomingSigners) {
        signer.setTradeSignature(entity); // Asocia el hijo al padre
        entity.getTradeSignerList().add(signer);
    }

    // Actualiza otros campos de la cabecera si es necesario
    entity.setOrigin(request.getOrigin());
    // ...

    repository.save(entity); // Guarda cabecera y detalle en cascada
} {
    
}

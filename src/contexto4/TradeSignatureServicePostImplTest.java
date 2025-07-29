package com.acelera.fx.digitalsignature.application.service;

import com.acelera.broker.fx.db.domain.port.ProductDocumentParametersRepositoryClient;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

@ExtendWith(MockitoExtension.class)
public class TradeSignatureServicePostImplTest {

    @Mock
    ProductDocumentParametersRepositoryClient productRepository;

    @InjectMocks
    TradeSignatureServicePostImpl impl;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();
}

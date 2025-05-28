package com.acelera.fx.db.infrastructure.adapter.persistence.jpa.repository;

import com.acelera.broker.fx.db.domain.dto.ProductDocumentParameters;
import com.acelera.fx.db.domain.port.persistence.ProductDocumentParametersRepository;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.crud.SpringJpaProductDocumentParametersRepository;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.mapper.ProductDocumentParametersMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class JpaProductDocumentParametersRepositoryImpl implements ProductDocumentParametersRepository {

    private final SpringJpaProductDocumentParametersRepository repository;

    @Override
    public List<ProductDocumentParameters> findProductDocumentParameters(String entity, String productId) {
        return repository.findByEntityAndProduct(entity, productId).stream()
                .map(ProductDocumentParametersMapper.INSTANCE::toProductDocumentParameters)
                .toList();
    }
}

public interface SpringJpaProductDocumentParametersRepository  extends CrudRepository<ProductDocumentParametersModel, Long> {
    List<ProductDocumentParametersModel> findByEntityAndProduct(String entity, String product);
}

@ExtendWith(MockitoExtension.class)
public class JpaProductDocumentParametersRepositoryImplTest {

    @InjectMocks
    private JpaProductDocumentParametersRepositoryImpl impl;

    @Mock
    private SpringJpaProductDocumentParametersRepository repository;

    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    @Test
    void testFindProductDocumentParametersJpa() {

        List<ProductDocumentParametersModel> response = new ArrayList<>();
        response.add(PODAM_FACTORY.manufacturePojo(ProductDocumentParametersModel.class));

        when(repository.findByEntityAndProduct(anyString(), anyString())).thenReturn(response);

        // when
        var optionalMarginResult = impl.findProductDocumentParameters(anyString(), anyString());

        // then
        assertFalse(optionalMarginResult.isEmpty());

    }
}
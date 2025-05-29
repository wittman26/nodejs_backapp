import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.fx.db.infrastructure.adapter.persistence.jpa.model.TradeSignatureModel;
import org.junit.jupiter.api.Test;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TradeSignatureMapperTest {
    private final TradeSignatureMapper MAPPER = TradeSignatureMapper.INSTANCE;

    private static final PodamFactoryImpl PODAM_FACTORY = new PodamFactoryImpl();

    @Test
    void testMapper_toDomain() {
        TradeSignatureModel input = PODAM_FACTORY.manufacturePojo(TradeSignatureModel.class);
        // El mapeo debe incluir la lista de signatarios y campos de auditoría si existen en el modelo
        TradeSignature result = MAPPER.toDomain(input);
        assertThat(result).as("TradeSignature").isNotNull();
        assertThat(result.getTradeSignatureId()).isEqualTo(input.getTradeSignatureId());
        assertThat(result.getEntity()).isEqualTo(input.getEntity());
        assertThat(result.getOriginId()).isEqualTo(input.getOriginId());
        assertThat(result.getOrigin()).isEqualTo(input.getOrigin());
        assertThat(result.getProductId()).isEqualTo(input.getProductId());
        assertThat(result.getSignatureType()).isEqualTo(input.getSignatureType());
        assertThat(result.getIndicatorSSCC()).isEqualTo(input.getIndicatorSSCC());
        assertThat(result.getValidatedBo()).isEqualTo(input.getValidatedBo());
        assertThat(result.getExpedientId()).isEqualTo(input.getExpedientId());
        // Si hay lista de signatarios
        if (input.getTradeSignerList() != null) {
            assertThat(result.getTradeSignerList()).hasSameSizeAs(input.getTradeSignerList());
        }
        // Si hay campos de auditoría
        assertThat(result.getFecalta()).isNotNull();
        assertThat(result.getFecmodi()).isNotNull();
    }

    @Test
    void testMapper_fromDomain() {
        TradeSignature input = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        TradeSignatureModel result = MAPPER.fromDomain(input);
        assertThat(result).as("TradeSignatureModel").isNotNull();
        assertThat(result.getTradeSignatureId()).isEqualTo(input.getTradeSignatureId());
        assertThat(result.getEntity()).isEqualTo(input.getEntity());
        assertThat(result.getOriginId()).isEqualTo(input.getOriginId());
        assertThat(result.getOrigin()).isEqualTo(input.getOrigin());
        assertThat(result.getProductId()).isEqualTo(input.getProductId());
        assertThat(result.getSignatureType()).isEqualTo(input.getSignatureType());
        assertThat(result.getIndicatorSSCC()).isEqualTo(input.getIndicatorSSCC());
        assertThat(result.getValidatedBo()).isEqualTo(input.getValidatedBo());
        assertThat(result.getExpedientId()).isEqualTo(input.getExpedientId());
        // Si hay lista de signatarios
        if (input.getTradeSignerList() != null) {
            assertThat(result.getTradeSignerList()).hasSameSizeAs(input.getTradeSignerList());
        }
        // Si hay campos de auditoría
        assertThat(result.getFecalta()).isNotNull();
        assertThat(result.getFecmodi()).isNotNull();
    }
}

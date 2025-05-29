
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.data.PersistWebFluxUtils;
import com.acelera.fx.db.domain.port.persistence.TradeSignatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Controller
@RequiredArgsConstructor
public class TradeSignatureRSocketController implements TradeSignatureRepositoryClient {

    private TradeSignatureRepository repository;

    @Override
    public Mono<TradeSignature> save(@Payload TradeSignature tradeSignature) {
        return PersistWebFluxUtils.save(() -> repository.save(tradeSignature))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<TradeSignature> findByParams(String entity, Integer originId, String productId) {
        return null;
    }

    @Override
    public Mono<TradeSignature> update(Integer tradeSignatureId, TradeSignature tradeSignature) {
        return null;
    }
}



import com.acelera.broker.fx.db.FxDbBrokerConfig;
import com.acelera.broker.fx.db.domain.dto.TradeSignature;
import com.acelera.broker.fx.db.domain.port.TradeSignatureRepositoryClient;
import com.acelera.broker.shared.RSocketRequesterBuilderLoggerAutoConfig;
import com.acelera.fx.db.domain.port.persistence.TradeSignatureRepository;
import com.acelera.rsocket.RSocketHeadersCustomizerAutoConfig;
import com.acelera.test.WithMockCustomUser;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketMessagingAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketRequesterAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketServerAutoConfiguration;
import org.springframework.boot.autoconfigure.rsocket.RSocketStrategiesAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import reactor.test.StepVerifier;
import uk.co.jemos.podam.api.PodamFactory;
import uk.co.jemos.podam.api.PodamFactoryImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@Execution(ExecutionMode.CONCURRENT)
@SpringBootTest(classes = {TradeSignatureRSocketController.class,  FxDbBrokerConfig.class,
        RSocketRequesterAutoConfiguration.class, RSocketMessagingAutoConfiguration.class, JacksonAutoConfiguration.class,
        RSocketHeadersCustomizerAutoConfig.class, RSocketRequesterBuilderLoggerAutoConfig.class,
        RSocketStrategiesAutoConfiguration.class, RSocketServerAutoConfiguration.class })
@WithMockCustomUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class TradeSignatureRSocketControllerTest {
    private static final int PORT = RandomUtils.nextInt(1111, 9999);
    private static final PodamFactory PODAM_FACTORY = new PodamFactoryImpl();

    private @MockBean TradeSignatureRepository repository;
    private @Autowired
    @Qualifier("tradeSignatureRepositoryClient") TradeSignatureRepositoryClient client;

    @DynamicPropertySource
    static void dynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("services.conac-springboot-fx-db-service.rsocket.host", () -> "localhost");
        registry.add("services.conac-springboot-fx-db-service.rsocket.port", () -> PORT);
        registry.add("spring.rsocket.server.port", () -> PORT);
    }

    @Test
    void testSave() {
        var tradeSignature = PODAM_FACTORY.manufacturePojo(TradeSignature.class);
        when(repository.save(any(TradeSignature.class))).thenReturn(tradeSignature);

        client
                .save(tradeSignature).as(StepVerifier::create).assertNext(value -> assertThat(value)
                        .as("save tradeSignature").usingRecursiveComparison().isEqualTo(tradeSignature))
                .verifyComplete();
    }
}


2025-05-29 17:10:10.181  INFO [conac-springboot-test-db,,] 18916 --- [actor-tcp-nio-2] com.acelera.util.ReactorUtils            : <------- END: Calling to TradeSignatureRepositoryClient.save

java.lang.AssertionError: [save tradeSignature] 
Expecting actual:
  TradeSignature(tradeSignatureId=866492556922500, entity=1V05uoq_0A, originId=866492528107100, origin=v9Naye9egO, productId=yYMS7q9011, signatureType=aUzalBMbnP, indicatorSSCC=V5pw9p8BFM, validatedBo=VyD86oTq7t, expedientId=866492527894700, tradeSignerList=[TradeSigner(tradeSignerId=866492540526200, tradeSignatureId=1359942194, documentType=xeLDTZp_iH, documentNumber=2zuNaCRpdS, signerId=AzbXRbPlHA, name=ZS8UMSgvtw, isClient=qvetfd_0b4, interventionType=rJ7nicKvGb), TradeSigner(tradeSignerId=866492544298300, tradeSignatureId=1023835544, documentType=AGnFhG5_k1, documentNumber=4CGSxeHmgC, signerId=s3wynjDOXk, name=oinirZ7D5g, isClient=JTvR_jbOPT, interventionType=sH_O0RYsDT), TradeSigner(tradeSignerId=866492547730800, tradeSignatureId=535804325, documentType=qMSiiJbXTU, documentNumber=gte70CG9_q, signerId=ENVg4a4vOg, name=FmUzbM3JAu, isClient=VHcSxbOdLB, interventionType=HmwPpOtsvx), TradeSigner(tradeSignerId=866492550486800, tradeSignatureId=1508202090, documentType=lbMTMKhmW0, documentNumber=iD8vgUC9Yy, signerId=7ca7dOBRbI, name=b8wBNxlN1V, isClient=4pPwUOJnj5, interventionType=X_4mOuELh5), TradeSigner(tradeSignerId=866492554738200, tradeSignatureId=1652018833, documentType=nFc8gXpNY_, documentNumber=PVHSQ1J9kz, signerId=hMIRSstjZc, name=Fzqdiq5zPa, isClient=pXlR24kcLJ, interventionType=wlDtRDRq_L)])
to be equal to:
  TradeSignature(tradeSignatureId=866492556922500, entity=1V05uoq_0A, originId=866492528107100, origin=v9Naye9egO, productId=yYMS7q9011, signatureType=aUzalBMbnP, indicatorSSCC=V5pw9p8BFM, validatedBo=VyD86oTq7t, expedientId=866492527894700, tradeSignerList=[TradeSigner(tradeSignerId=866492540526200, tradeSignatureId=1359942194, documentType=xeLDTZp_iH, documentNumber=2zuNaCRpdS, signerId=AzbXRbPlHA, name=ZS8UMSgvtw, isClient=qvetfd_0b4, interventionType=rJ7nicKvGb), TradeSigner(tradeSignerId=866492544298300, tradeSignatureId=1023835544, documentType=AGnFhG5_k1, documentNumber=4CGSxeHmgC, signerId=s3wynjDOXk, name=oinirZ7D5g, isClient=JTvR_jbOPT, interventionType=sH_O0RYsDT), TradeSigner(tradeSignerId=866492547730800, tradeSignatureId=535804325, documentType=qMSiiJbXTU, documentNumber=gte70CG9_q, signerId=ENVg4a4vOg, name=FmUzbM3JAu, isClient=VHcSxbOdLB, interventionType=HmwPpOtsvx), TradeSigner(tradeSignerId=866492550486800, tradeSignatureId=1508202090, documentType=lbMTMKhmW0, documentNumber=iD8vgUC9Yy, signerId=7ca7dOBRbI, name=b8wBNxlN1V, isClient=4pPwUOJnj5, interventionType=X_4mOuELh5), TradeSigner(tradeSignerId=866492554738200, tradeSignatureId=1652018833, documentType=nFc8gXpNY_, documentNumber=PVHSQ1J9kz, signerId=hMIRSstjZc, name=Fzqdiq5zPa, isClient=pXlR24kcLJ, interventionType=wlDtRDRq_L)])
when recursively comparing field by field, but found the following 12 differences:

field/property 'fecalta' differ:
- actual value  : 2025-05-29T15:10:09.417991200Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.417991200+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'fecmodi' differ:
- actual value  : 2025-05-29T15:10:09.417991200Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.417991200+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'tradeSignerList[0].fecalta' differ:
- actual value  : 2025-05-29T15:10:09.402257500Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.402257500+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'tradeSignerList[0].fecmodi' differ:
- actual value  : 2025-05-29T15:10:09.402257500Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.402257500+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'tradeSignerList[1].fecalta' differ:
- actual value  : 2025-05-29T15:10:09.402257500Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.402257500+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'tradeSignerList[1].fecmodi' differ:
- actual value  : 2025-05-29T15:10:09.402257500Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.402257500+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'tradeSignerList[2].fecalta' differ:
- actual value  : 2025-05-29T15:10:09.402257500Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.402257500+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'tradeSignerList[2].fecmodi' differ:
- actual value  : 2025-05-29T15:10:09.402257500Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.402257500+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'tradeSignerList[3].fecalta' differ:
- actual value  : 2025-05-29T15:10:09.402257500Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.402257500+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'tradeSignerList[3].fecmodi' differ:
- actual value  : 2025-05-29T15:10:09.402257500Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.402257500+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'tradeSignerList[4].fecalta' differ:
- actual value  : 2025-05-29T15:10:09.414934500Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.414934500+02:00[Europe/Madrid] (java.time.ZonedDateTime)

field/property 'tradeSignerList[4].fecmodi' differ:
- actual value  : 2025-05-29T15:10:09.417991200Z[UTC] (java.time.ZonedDateTime)
- expected value: 2025-05-29T17:10:09.417991200+02:00[Europe/Madrid] (java.time.ZonedDateTime)
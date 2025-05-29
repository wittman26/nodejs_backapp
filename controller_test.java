package com.acelera.fx.db.infrastructure.adapter.rsocket.controller;

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


2025-05-29 17:04:37.537  INFO [conac-springboot-test-db,,] 872 --- [           main] .r.c.TradeSignatureRSocketControllerTest : Starting TradeSignatureRSocketControllerTest using Java 17.0.6 on SDSESCWEQD3EPTW with PID 872 (started by x612243 in C:\GIT_REPOS\acelera\conac-springboot-fx-db)
2025-05-29 17:04:37.539  INFO [conac-springboot-test-db,,] 872 --- [           main] .r.c.TradeSignatureRSocketControllerTest : The following 1 profile is active: "test"
2025-05-29 17:04:40.790  INFO [conac-springboot-test-db,,] 872 --- [           main] o.s.b.rsocket.netty.NettyRSocketServer   : Netty RSocket started on port(s): 4062
2025-05-29 17:04:40.799  INFO [conac-springboot-test-db,,] 872 --- [           main] .r.c.TradeSignatureRSocketControllerTest : Started TradeSignatureRSocketControllerTest in 5.219 seconds (JVM running for 8.246)
2025-05-29 17:04:41.116  INFO [conac-springboot-test-db,,] 872 --- [oundedElastic-1] com.acelera.util.ReactorUtils            : -------> INI: Calling to TradeSignatureRepositoryClient.save
2025-05-29 17:04:41.485  INFO [conac-springboot-test-db,,] 872 --- [actor-tcp-nio-2] c.a.b.shared.domain.RSocketProxyLogs     : client: {"correlationId":1748531081451276461,"route":"trade-signature.save","messageType":"request","rsocketType":"requestResponse","payload":{"usualta":"REge577o9v","fecalta":"2025-05-29T17:04:40.9813255+02:00","usumodi":"YZdXzZUuKR","fecmodi":"2025-05-29T17:04:40.9813255+02:00","tradeSignatureId":866164123392000,"entity":"lLjTJIcYol","originId":866164096727700,"origin":"y06Xfxufzd","productId":"Tzu63J9dgo","signatureType":"8chCTolj6q","indicatorSSCC":"fFlyCzO2x4","validatedBo":"iUJMADkMUF","expedientId":866164096578300,"tradeSignerList":[{"usualta":"67fhAv6KFf","fecalta":"2025-05-29T17:04:40.9608312+02:00","usumodi":"Bht5CXOcbg","fecmodi":"2025-05-29T17:04:40.9608312+02:00","tradeSignerId":866164108216400,"tradeSignatureId":1594284466,"documentType":"mifxppoGmU","documentNumber":"hAP3XojcDw","signerId":"G5a9EkOU_H","name":"V5ixhhLN5H","isClient":"2nOYTeUZ3b","interventionType":"0EnW_qlXBi"},{"usualta":"QurGt5T4zu","fecalta":"2025-05-29T17:04:40.9711967+02:00","usumodi":"mxvro2br9x","fecmodi":"2025-05-29T17:04:40.9711967+02:00","tradeSignerId":866164111717000,"tradeSignatureId":186029698,"documentType":"VXFjTGk3uM","documentNumber":"2f801dHWw3","signerId":"w05oKyayfJ","name":"0L35SgzSTk","isClient":"L6FL8xOWqh","interventionType":"dlnT5m1zjz"},{"usualta":"uhiZ48wU2V","fecalta":"2025-05-29T17:04:40.9742751+02:00","usumodi":"acPHDuwZW_","fecmodi":"2025-05-29T17:04:40.9742751+02:00","tradeSignerId":866164114784300,"tradeSignatureId":735128738,"documentType":"K7LxjXYfpM","documentNumber":"u0QjKNcCB4","signerId":"zpEq8GdiOh","name":"gCGIFy6cJi","isClient":"sz8FraWvEP","interventionType":"eui1PNDQ4o"},{"usualta":"Hr7HKj5q1v","fecalta":"2025-05-29T17:04:40.9742751+02:00","usumodi":"ndBbr4iPdf","fecmodi":"2025-05-29T17:04:40.9742751+02:00","tradeSignerId":866164117702700,"tradeSignatureId":560510020,"documentType":"zibkxD8KCj","documentNumber":"M4r3aLP8Y5","signerId":"F_dRhLoO05","name":"y3NkykewtR","isClient":"mt2Oj4_ydC","interventionType":"XYH94eHOT3"},{"usualta":"Ldgx5G6OOS","fecalta":"2025-05-29T17:04:40.9813255+02:00","usumodi":"iGJ77VFMk_","fecmodi":"2025-05-29T17:04:40.9813255+02:00","tradeSignerId":866164121462500,"tradeSignatureId":47922809,"documentType":"lKg4sg4XwE","documentNumber":"WVC7zS74oC","signerId":"mvXhJ4xKMy","name":"5w9rlymixE","isClient":"b7IEa9Sv2c","interventionType":"4WWAPHrF3b"}]},"metadata":{"acelera.ctx":{"entity":"0049","locale":"es_ES"}}}
2025-05-29 17:04:41.557 ERROR [conac-springboot-test-db,,] 872 --- [oundedElastic-2] o.s.m.h.i.reactive.InvocableHelper       : No exception handling method

java.lang.NullPointerException: Cannot invoke "com.acelera.fx.db.domain.port.persistence.TradeSignatureRepository.save(com.acelera.broker.fx.db.domain.dto.TradeSignature)" because "this.repository" is null
	at com.acelera.fx.db.infrastructure.adapter.rsocket.controller.TradeSignatureRSocketController.lambda$save$0(TradeSignatureRSocketController.java:21) ~[classes/:na]
	at com.acelera.data.PersistWebFluxUtils.lambda$save$1(PersistWebFluxUtils.java:29) ~[classes/:na]
	at reactor.core.publisher.FluxMap$MapSubscriber.onNext(FluxMap.java:106) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.FluxSwitchIfEmpty$SwitchIfEmptySubscriber.onNext(FluxSwitchIfEmpty.java:74) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.Operators$MonoSubscriber.complete(Operators.java:1839) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.MonoFlatMap$FlatMapMain.onNext(MonoFlatMap.java:151) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.FluxFilterFuseable$FilterFuseableSubscriber.onNext(FluxFilterFuseable.java:118) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.Operators$ScalarSubscription.request(Operators.java:2400) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.FluxFilterFuseable$FilterFuseableSubscriber.request(FluxFilterFuseable.java:191) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.MonoFlatMap$FlatMapMain.onSubscribe(MonoFlatMap.java:110) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.FluxFilterFuseable$FilterFuseableSubscriber.onSubscribe(FluxFilterFuseable.java:87) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.MonoCurrentContext.subscribe(MonoCurrentContext.java:36) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.Mono.subscribe(Mono.java:4490) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.publisher.MonoSubscribeOn$SubscribeOnSubscriber.run(MonoSubscribeOn.java:126) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.scheduler.WorkerTask.call(WorkerTask.java:84) ~[reactor-core-3.4.34.jar:3.4.34]
	at reactor.core.scheduler.WorkerTask.call(WorkerTask.java:37) ~[reactor-core-3.4.34.jar:3.4.34]
	at java.base/java.util.concurrent.FutureTask.run(FutureTask.java:264) ~[na:na]
	at java.base/java.util.concurrent.ScheduledThreadPoolExecutor$ScheduledFutureTask.run(ScheduledThreadPoolExecutor.java:304) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1136) ~[na:na]
	at java.base/java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:635) ~[na:na]
	at java.base/java.lang.Thread.run(Thread.java:833) ~[na:na]

2025-05-29 17:04:41.563 ERROR [conac-springboot-test-db,,] 872 --- [actor-tcp-nio-2] c.a.b.shared.domain.RSocketProxyLogs     : <------- RSocket client error of correlation 1748531081451276461:

io.rsocket.exceptions.ApplicationErrorException: Cannot invoke "com.acelera.fx.db.domain.port.persistence.TradeSignatureRepository.save(com.acelera.broker.fx.db.domain.dto.TradeSignature)" because "this.repository" is null
	at io.rsocket.exceptions.Exceptions.from(Exceptions.java:76) ~[rsocket-core-1.1.3.jar:na]
	at io.rsocket.core.RSocketRequester.handleFrame(RSocketRequester.java:261) ~[rsocket-core-1.1.3.jar:na]
	at io.rs
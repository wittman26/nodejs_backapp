package com.acelera.rest.fx.infrastructure.config;

import com.acelera.rest.fx.domain.port.FxCaller;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.web.reactive.function.client.WebClient;

import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(classes = { FxConfig.class})
public class FxConfigTest {
    @TestConfiguration
    public static class EarlyConfiguration {
        @Bean
        WebClient webClientLogger() {
            return WebClient.builder().build();
        }
    }

    private @Autowired FxCaller caller;

    @Test
    void test() {
        assertNotNull(caller);
    }
}

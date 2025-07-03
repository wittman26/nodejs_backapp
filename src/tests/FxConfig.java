package com.acelera.rest.fx.infrastructure.config;

import com.acelera.rest.fx.domain.port.FxCaller;
import com.acelera.rest.fx.infrastructure.FxCallerImpl;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.rsocket.exceptions.ApplicationErrorException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Configuration
@RequiredArgsConstructor
public class FxConfig {
    private final WebClient webClientLogger;

    @Value("${conac-springboot-fx-service.url:http://conac-springboot-fx-service:8080}")
    private String basePath;

    //@formatter:off
    @Bean
    WebClient webClientFx() {
        return webClientLogger.mutate().baseUrl(basePath)
                .filter(ExchangeFilterFunction.ofResponseProcessor(this::transform)).build();
    }
    //@formatter:on

    @Bean
    FxCaller fxCallerImpl(WebClient webClientFx) {
        return new FxCallerImpl(webClientFx);
    }

    private Mono<ClientResponse> transform(ClientResponse clientResponse) {
        if (clientResponse.statusCode().isError()) {
            return filterHttpMessage(clientResponse).switchIfEmpty(Mono.just(clientResponse));
        }

        return Mono.just(clientResponse);
    }

    private Mono<ClientResponse> filterHttpMessage(ClientResponse clientResponse) {
        return clientResponse.bodyToMono(ObjectNode.class).filter(x -> x.has("httpMessage"))
                .map(x -> x.get("httpMessage").asText()).flatMap(x -> Mono.error(new ApplicationErrorException(x)));
    }
}

package com.acelera.rest.dfd.infrastructure.adapter.external;

import com.acelera.broker.rest.dfd.domain.ExpedientRequest;
import com.acelera.rest.dfd.domain.port.external.DfdCaller;
import com.acelera.rest.mifid.motor.infrastructure.adapter.external.MifidMotorCallerImpl;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class DfdCallerImpl implements DfdCaller {

    private final WebClient webClient;

    @Override
    public Mono<Long> createExpedient(ExpedientRequest request) {
                return webClient.post().uri(uriBuilder -> uriBuilder.path("/v1/expedients").build())
                .bodyValue(request)
                .headers(httpHeaders -> {
//                    httpHeaders.setBearerAuth(getCleanToken(request.getToken()));
                    httpHeaders.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, extractError())
                .onStatus(HttpStatusCode::is5xxServerError, extractError2())
                .bodyToMono(Long.class)
                .onErrorMap(Exception.class, e -> {
                    log.error("An error occurred while trying to CREATING Expedient.: {} - {}",
                            e.getClass().getSimpleName(), e.getMessage(), e);
                    return e;
                });
    }

    private Function<ClientResponse, Mono<? extends Throwable>> extractError() {
        return response -> response.bodyToMono(DfdCallerImpl.MifidErrorResponse.class).flatMap(err -> {

            if (!CollectionUtils.isEmpty(err.getFields())) {
                return Mono.error(() -> new IllegalArgumentException(
                        err.getFields().stream().map(MifidErrorResponse.FieldError::getMessage).collect(
                                Collectors.joining(", "))));
            }

            if (StringUtils.isNotBlank(err.getMessage())) {
                return Mono.error(() -> new IllegalArgumentException(err.getMessage()));
            }

            return response.createException().flatMap(Mono::error);
        });
    }

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MifidErrorResponse {
        private String message;
        private List<FieldError> fields;

        @Data
        @NoArgsConstructor
        @JsonIgnoreProperties(ignoreUnknown = true)
        private static class FieldError {
            private String error;
            private String message;
        }
    }

}

package com.acelera.gateway.antihacking.filter.sanitization.parser;

import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.acelera.gateway.antihacking.filter.sanitization.SanitizeConfig;
import com.acelera.gateway.antihacking.security.sanitize.validation.ValidatorProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * Headers Validator
 */
@Slf4j
@Component
public class HeadersSanitizeValidator {

    private final ValidatorProvider validatorProvider;

    /**
     * Headers validator constructor
     *
     * @param validatorProvider validator provider
     */
    public HeadersSanitizeValidator(ValidatorProvider validatorProvider) {
        Assert.notNull(validatorProvider, "validatorProvider must not be null");
        this.validatorProvider = validatorProvider;
    }

    /**
     * Has errors
     *
     * @param config  config
     * @param request request
     * @return error
     */
    public boolean hasErrors(SanitizeConfig config, ServerHttpRequest request) {
        return hasErrors(config, request, null);
    }

    /**
     * Has errors
     *
     * @param config        config
     * @param request       request
     * @param headerIgnored headers to ignore
     * @return error
     */
    public boolean hasErrors(SanitizeConfig config, ServerHttpRequest request, List<String> headerIgnored) {

        log.debug("[SERVER-CFG] Testing headers  ...");

        return Optional.of(request.getHeaders()).orElse(new HttpHeaders()).entrySet().stream()
                .anyMatch(entry -> sanitize(entry, config, headerIgnored));
    }

    private boolean sanitize(Entry<String, List<String>> entry, SanitizeConfig config, List<String> headerIgnored) {
        String name = entry.getKey();

        if (headerIgnored != null && headerIgnored.stream().anyMatch(header -> header.equalsIgnoreCase(name))) {
            log.debug("'{}' header ignored.", name);
            return false;
        }

        List<String> values = entry.getValue();
        List<String> rules = config.getHeaders().containsKey(name) ? config.getHeaders().get(name)
                : config.getHeaders().get("default");
        return Optional.ofNullable(values).orElse(Collections.emptyList()).stream().anyMatch(
                value -> Optional.ofNullable(rules).orElse(Collections.emptyList()).stream().anyMatch(rule -> {
                    log.debug("Sanitization header: '{}' " + "with value: '{}'  with rule '{}'", name, value, rule);
                    try {
                        validatorProvider.sanitizeString("header: " + name, value, rule);
                    } catch (IllegalArgumentException e) {
                        log.error("Found an error " + "iterating over query params... {}", e.getMessage(), e);
                        return true;
                    }
                    return false;
                }));
    }
}

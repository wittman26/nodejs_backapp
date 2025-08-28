package com.acelera.gateway.antihacking.filter.sanitization.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.LinkedMultiValueMap;

import com.acelera.gateway.antihacking.filter.sanitization.SanitizeConfig;
import com.acelera.gateway.antihacking.security.sanitize.validation.ValidatorProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * Query params Validator
 */
@Slf4j
@Component
public class QueryParamsSanitizeValidator {

    private final ValidatorProvider validatorProvider;

    /**
     * Query params validator constructor
     *
     * @param validatorProvider validator provider
     */
    public QueryParamsSanitizeValidator(ValidatorProvider validatorProvider) {
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

        log.debug("[SERVER-CFG] Testing query params  ...");

        return Optional.of(request.getQueryParams()).orElse(new LinkedMultiValueMap<>()).entrySet().stream()
                .anyMatch(entry -> sanitize(entry, config));
    }

    private boolean sanitize(Entry<String, List<String>> entry, SanitizeConfig config) {
        String name = entry.getKey();
        List<String> values = entry.getValue();
        List<String> rules = config.getQueryString().containsKey(name) ? config.getQueryString().get(name)
                : config.getQueryString().get("default");
        return Optional.ofNullable(values).orElse(Collections.emptyList()).stream().anyMatch(
                param -> Optional.ofNullable(rules).orElse(Collections.emptyList()).stream().anyMatch(rule -> {
                    try {
                        String value = URLDecoder.decode(param, "UTF-8");
                        log.debug("Sanitization parameter " + "'{}' with value '{}' with rule '{}'", name, value, rule);
                        validatorProvider.sanitizeString("parameter: " + name, value, rule);
                    } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                        log.error("Found an error " + "iterating over query params... {}", e.getMessage(), e);
                        return true;
                    }
                    return false;
                }));
    }
}

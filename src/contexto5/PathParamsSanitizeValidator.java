package com.acelera.gateway.antihacking.filter.sanitization.parser;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import com.acelera.gateway.antihacking.filter.sanitization.SanitizeConfig;
import com.acelera.gateway.antihacking.security.sanitize.validation.ValidatorProvider;

import lombok.extern.slf4j.Slf4j;

/**
 * Path params Validator
 */
@Slf4j
@Component
public class PathParamsSanitizeValidator {

    private final ValidatorProvider validatorProvider;

    /**
     * Path params validator constructor
     *
     * @param validatorProvider validator provider
     */
    public PathParamsSanitizeValidator(ValidatorProvider validatorProvider) {
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

        log.debug("[SERVER-CFG] Testing paths  ...");

        return Optional.of(request.getPath().elements()).orElse(Collections.emptyList()).stream()
                .filter(element -> !("/").equals(element.value())).anyMatch(element -> {
                    List<String> rules = config.getPathVariable().get("default");
                    return Optional.ofNullable(rules).orElse(Collections.emptyList()).stream().anyMatch(rule -> {
                        try {
                            String value = URLDecoder.decode(element.value(), "UTF-8");
                            log.debug("Sanitization parameterPath '{}' with rule '{}'", value, rule);
                            validatorProvider.sanitizeString("parameterPath: " + value, value, rule);
                        } catch (IllegalArgumentException | UnsupportedEncodingException e) {
                            log.error("Found an error iterating over path parameters... {}", e.getMessage(), e);
                            return true;
                        }
                        return false;
                    });
                });
    }
}

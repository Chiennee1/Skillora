package com.example.skillora_platform.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "skillora.ai.gemini")
public record GeminiProperties(
        String apiKey,
        String model,
        String baseUrl,
        Double temperature,
        Integer maxOutputTokens
) {

    private static final String DEFAULT_MODEL = "gemini-2.5-flash";
    private static final String DEFAULT_BASE_URL = "https://generativelanguage.googleapis.com/v1beta";
    private static final double DEFAULT_TEMPERATURE = 0.4;
    private static final int DEFAULT_MAX_OUTPUT_TOKENS = 1024;

    public String resolvedModel() {
        if (model == null || model.isBlank()) {
            return DEFAULT_MODEL;
        }
        return model;
    }

    public String resolvedBaseUrl() {
        if (baseUrl == null || baseUrl.isBlank()) {
            return DEFAULT_BASE_URL;
        }
        return baseUrl;
    }

    public double resolvedTemperature() {
        if (temperature == null) {
            return DEFAULT_TEMPERATURE;
        }
        return temperature;
    }

    public int resolvedMaxOutputTokens() {
        if (maxOutputTokens == null) {
            return DEFAULT_MAX_OUTPUT_TOKENS;
        }
        return maxOutputTokens;
    }
}

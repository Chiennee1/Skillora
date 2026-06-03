package com.example.skillora_platform.chat.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import com.fasterxml.jackson.databind.JsonNode;
import com.example.skillora_platform.config.GeminiProperties;
import com.example.skillora_platform.chat.entity.ChatRole;
import com.example.skillora_platform.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultGeminiClient implements GeminiClient {

    private final GeminiProperties geminiProperties;
    private final RestClient.Builder restClientBuilder;

    @Override
    public GeminiReply generate(String systemInstruction, List<GeminiMessage> messages) {
        requireConfigured();

        Map<String, Object> requestBody = Map.of(
                "systemInstruction", Map.of("parts", List.of(Map.of("text", systemInstruction))),
                "contents", toContents(messages),
                "generationConfig", Map.of(
                        "temperature", geminiProperties.resolvedTemperature(),
                        "maxOutputTokens", geminiProperties.resolvedMaxOutputTokens()
                )
        );

        try {
            JsonNode response = restClientBuilder.baseUrl(geminiProperties.resolvedBaseUrl())
                    .build()
                    .post()
                    .uri("/models/{model}:generateContent", geminiProperties.resolvedModel())
                    .header("x-goog-api-key", geminiProperties.apiKey())
                    .body(requestBody)
                    .retrieve()
                    .body(JsonNode.class);
            return parseResponse(response);
        } catch (RestClientException ex) {
            throw new BusinessException("Gemini AI request failed", HttpStatus.BAD_GATEWAY);
        }
    }

    private void requireConfigured() {
        if (geminiProperties.apiKey() == null || geminiProperties.apiKey().isBlank()) {
            throw new BusinessException("Gemini AI is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private List<Map<String, Object>> toContents(List<GeminiMessage> messages) {
        List<Map<String, Object>> contents = new ArrayList<>();
        for (GeminiMessage message : messages) {
            if (message.role() == ChatRole.SYSTEM) {
                continue;
            }
            contents.add(Map.of(
                    "role", message.role() == ChatRole.ASSISTANT ? "model" : "user",
                    "parts", List.of(Map.of("text", message.content()))
            ));
        }
        return contents;
    }

    private GeminiReply parseResponse(JsonNode response) {
        String text = extractText(response);
        if (text == null || text.isBlank()) {
            throw new BusinessException("Gemini AI returned an empty response", HttpStatus.BAD_GATEWAY);
        }
        JsonNode totalTokensNode = response.at("/usageMetadata/totalTokenCount");
        Integer tokensUsed = totalTokensNode.isNumber() ? totalTokensNode.asInt() : null;
        return new GeminiReply(text, geminiProperties.resolvedModel(), tokensUsed);
    }

    private String extractText(JsonNode response) {
        if (response == null) {
            return null;
        }
        JsonNode parts = response.at("/candidates/0/content/parts");
        if (!parts.isArray()) {
            return null;
        }
        StringBuilder text = new StringBuilder();
        for (JsonNode part : parts) {
            String value = part.path("text").asText(null);
            if (value != null && !value.isBlank()) {
                text.append(value);
            }
        }
        return text.toString();
    }
}

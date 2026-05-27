package com.example.skillora_platform.course.service;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.skillora_platform.config.BunnyStreamProperties;
import com.example.skillora_platform.course.dto.BunnyVideoCreated;
import com.example.skillora_platform.exception.BusinessException;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DefaultBunnyStreamClient implements BunnyStreamClient {

    private static final String BUNNY_STREAM_BASE_URL = "https://video.bunnycdn.com";

    private final BunnyStreamProperties bunnyStreamProperties;
    private final RestClient.Builder restClientBuilder;

    @Override
    public BunnyVideoCreated createVideo(String title) {
        requireConfigured();
        BunnyCreateVideoResponse response = restClientBuilder.build()
                .post()
                .uri("%s/library/%s/videos".formatted(
                        BUNNY_STREAM_BASE_URL,
                        bunnyStreamProperties.libraryId()
                ))
                .header("AccessKey", bunnyStreamProperties.apiKey())
                .body(Map.of("title", title))
                .retrieve()
                .body(BunnyCreateVideoResponse.class);

        if (response == null || response.guid() == null || response.guid().isBlank()) {
            throw new BusinessException("Bunny Stream did not return a video id", HttpStatus.BAD_GATEWAY);
        }
        return new BunnyVideoCreated(response.guid());
    }

    private void requireConfigured() {
        if (bunnyStreamProperties.libraryId() == null || bunnyStreamProperties.libraryId().isBlank()
                || bunnyStreamProperties.apiKey() == null || bunnyStreamProperties.apiKey().isBlank()) {
            throw new BusinessException("Bunny Stream is not configured", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private record BunnyCreateVideoResponse(String guid) {
    }
}

package com.example.skillora_platform.course.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.course.service.LessonVideoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class VideoWebhookController {

    private final LessonVideoService lessonVideoService;

    @PostMapping(Constants.API_V1_PREFIX + "/videos/bunny/webhook")
    public ResponseEntity<ApiResponse<Void>> bunnyWebhook(
            @RequestBody String rawBody,
            @RequestHeader(name = "X-BunnyStream-Signature-Version", required = false) String signatureVersion,
            @RequestHeader(name = "X-BunnyStream-Signature-Algorithm", required = false) String signatureAlgorithm,
            @RequestHeader(name = "X-BunnyStream-Signature", required = false) String signature
    ) {
        lessonVideoService.handleBunnyWebhook(rawBody, signatureVersion, signatureAlgorithm, signature);
        return ResponseEntity.ok(ApiResponse.success("Bunny Stream webhook received"));
    }
}

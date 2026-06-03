package com.example.skillora_platform.chat.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.skillora_platform.chat.dto.ChatAskRequest;
import com.example.skillora_platform.chat.dto.ChatConversationResponse;
import com.example.skillora_platform.chat.dto.ChatMessageResponse;
import com.example.skillora_platform.chat.dto.ChatResponse;
import com.example.skillora_platform.chat.service.ChatbotService;
import com.example.skillora_platform.common.ApiResponse;
import com.example.skillora_platform.common.Constants;
import com.example.skillora_platform.common.PageResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping(Constants.CHAT_API_PREFIX)
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class ChatController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<ApiResponse<ChatResponse>> ask(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody ChatAskRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Chat response generated successfully",
                        chatbotService.ask(request, jwt.getSubject())));
    }

    @GetMapping("/conversations")
    public ApiResponse<PageResponse<ChatConversationResponse>> conversations(
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size
    ) {
        return ApiResponse.success(chatbotService.listConversations(jwt.getSubject(), page, size));
    }

    @GetMapping("/conversations/{id}/messages")
    public ApiResponse<PageResponse<ChatMessageResponse>> messages(
            @PathVariable("id") @Positive Long id,
            @AuthenticationPrincipal Jwt jwt,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "" + Constants.DEFAULT_PAGE_SIZE) int size
    ) {
        return ApiResponse.success(chatbotService.listMessages(id, jwt.getSubject(), page, size));
    }
}

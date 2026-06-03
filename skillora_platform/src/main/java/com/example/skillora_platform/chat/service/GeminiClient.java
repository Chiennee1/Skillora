package com.example.skillora_platform.chat.service;

import java.util.List;

public interface GeminiClient {

    GeminiReply generate(String systemInstruction, List<GeminiMessage> messages);
}

package com.forgeai.backend.dto.ai;

import java.util.List;

public record OpenRouterRequest(
        String model,
        List<Message> messages) {
    public record Message(String role, String content) {
    }

    public static OpenRouterRequest defaultRequest(String prompt) {
        return new OpenRouterRequest(
                "deepseek/deepseek-chat",
                List.of(new Message("user", prompt)));
    }
}

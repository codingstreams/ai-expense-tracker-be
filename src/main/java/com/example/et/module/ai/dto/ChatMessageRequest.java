package com.example.et.module.ai.dto;

public record ChatMessageRequest(
    String message,
    String sessionId
) {
}

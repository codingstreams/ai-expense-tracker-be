package com.example.et.service.ai.chat;

import org.springframework.ai.chat.messages.MessageType;

public record StoredChatMessage(
    MessageType type,
    String content
) {}

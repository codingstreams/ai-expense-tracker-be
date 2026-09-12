package com.example.et.module.ai.chat;

import org.springframework.ai.chat.messages.MessageType;

public record StoredChatMessage(
    MessageType type,
    String content
) {
}

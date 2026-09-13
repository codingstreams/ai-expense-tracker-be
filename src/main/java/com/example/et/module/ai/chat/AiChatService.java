package com.example.et.module.ai.chat;

import com.example.et.module.ai.dto.ChatMessageRequest;
import com.example.et.module.ai.dto.ChatReplyResponse;

public interface AiChatService {
  ChatReplyResponse chat(String userId, ChatMessageRequest request);

  void clearSession(String userId);
}

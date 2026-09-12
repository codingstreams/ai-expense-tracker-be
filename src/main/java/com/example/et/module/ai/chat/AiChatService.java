package com.example.et.module.ai.chat;

import com.example.et.module.ai.dto.AiChatRequestDto;
import com.example.et.module.ai.dto.AiChatResponseDto;

public interface AiChatService {
  AiChatResponseDto chat(String userId, AiChatRequestDto request);

  void clearSession(String userId);
}

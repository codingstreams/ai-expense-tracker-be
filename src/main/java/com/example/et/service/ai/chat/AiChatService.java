package com.example.et.service.ai.chat;

import com.example.et.controller.dto.ai.AiChatRequestDto;
import com.example.et.controller.dto.ai.AiChatResponseDto;

public interface AiChatService {
  AiChatResponseDto chat(String userId, AiChatRequestDto request);
}

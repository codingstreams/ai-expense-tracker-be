package com.example.et.service.ai.chat;

import com.example.et.controller.dto.AiChatRequestDto;
import com.example.et.controller.dto.AiChatResponseDto;

public interface AiChatService {
  AiChatResponseDto chat(String userId, AiChatRequestDto request);
}

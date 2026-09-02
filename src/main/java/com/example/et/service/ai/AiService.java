package com.example.et.service.ai;

import com.example.et.controller.dto.AiInputDto;
import com.example.et.controller.dto.AiInsightDto;
import com.example.et.controller.dto.AiTaskDto;
import com.example.et.model.ai.AiParsingTask;

public interface AiService {
  AiTaskDto save(String userId, AiInputDto requestBody);

  void parse(AiParsingTask task);

  AiInsightDto getLatestInsight(String userId);
}

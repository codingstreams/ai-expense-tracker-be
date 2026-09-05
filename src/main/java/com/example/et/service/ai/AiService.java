package com.example.et.service.ai;

import com.example.et.controller.dto.ai.AiInputDto;
import com.example.et.controller.dto.ai.AiInsightDto;
import com.example.et.controller.dto.ai.AiTaskDto;
import com.example.et.model.ai.AiParsingTask;

public interface AiService {
  AiTaskDto save(String userId, AiInputDto requestBody);

  void parse(AiParsingTask task);

  AiInsightDto getLatestInsight(String userId);

  AiInsightDto generateInsights(String userId);
}

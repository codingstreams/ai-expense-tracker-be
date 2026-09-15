package com.example.et.module.ai;

import com.example.et.module.ai.dto.AiInputDto;
import com.example.et.module.ai.dto.AiInsightDto;
import com.example.et.module.ai.dto.AiTaskDto;
import com.example.et.module.ai.parser.AiParsingTask;

public interface AiService {
  AiTaskDto save(String userId, AiInputDto requestBody);

  void parse(AiParsingTask task);

  AiInsightDto getLatestInsight(String userId);

  AiInsightDto generateInsights(String userId);
}

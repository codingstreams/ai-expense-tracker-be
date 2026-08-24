package com.example.et.service.ai;

import com.example.et.controller.dto.AiInputDto;
import com.example.et.controller.dto.AiTaskDto;
import com.example.et.model.ai.AiParsingTask;

public interface AiService {
  AiTaskDto save(String appUserId, AiInputDto requestBody);

  void parse(AiParsingTask task);
}

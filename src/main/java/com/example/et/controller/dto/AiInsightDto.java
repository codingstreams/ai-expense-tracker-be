package com.example.et.controller.dto;

import java.time.LocalDateTime;
import java.util.List;

public record AiInsightDto(
    String period,
    LocalDateTime generatedAt,
    String summary,
    TopCategory topSpendingCategory,
    List<String> anomalies,
    List<String> actionableTips
) {
  public record TopCategory(
      String category,
      Double percentage,
      String insight
  ) {}
}

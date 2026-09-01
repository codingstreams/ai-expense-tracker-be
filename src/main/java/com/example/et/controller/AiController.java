package com.example.et.controller;


import com.example.et.controller.dto.AiInputDto;
import com.example.et.controller.dto.AiInsightDto;
import com.example.et.controller.dto.AiTaskDto;
import com.example.et.service.ai.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
  private final AiService aiService;

  @PostMapping("/parse-tasks")
  public ResponseEntity<AiTaskDto> parseRawText(@RequestBody AiInputDto requestBody,
                                                @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(aiService.save(userId, requestBody));
  }

  @GetMapping("/insights")
  public ResponseEntity<AiInsightDto> getLatestInsight(@AuthenticationPrincipal String userId) {
    final var latest = aiService.getLatestInsight(userId);
    return latest != null ? ResponseEntity.ok(latest) : ResponseEntity.noContent().build();
  }

  @PostMapping("/insights/generate")
  public ResponseEntity<AiInsightDto> generateInsights(@AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(aiService.generateInsights(userId));
  }

}

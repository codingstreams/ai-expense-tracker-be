package com.example.et.controller;


import com.example.et.controller.dto.*;
import com.example.et.service.ai.AiService;
import com.example.et.service.ai.chat.AiChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {
  private final AiService aiService;
  private final AiChatService aiChatService;

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

  @PostMapping("/chat")
  public ResponseEntity<AiChatResponseDto> chat(@RequestBody AiChatRequestDto request,
                                                @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(aiChatService.chat(userId, request));
  }
  @PostMapping("/insights/generate")
  public ResponseEntity<AiInsightDto> generateInsights(@AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(aiService.generateInsights(userId));
  }

}

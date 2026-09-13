package com.example.et.module.ai;


import com.example.et.module.ai.chat.AiChatService;
import com.example.et.module.ai.dto.*;
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
  public ResponseEntity<ChatReplyResponse> chat(@RequestBody ChatMessageRequest request,
                                                @AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(aiChatService.chat(userId, request));
  }

  @DeleteMapping("/chat")
  public ResponseEntity<Void> clearChat(@AuthenticationPrincipal String userId) {
    aiChatService.clearSession(userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/chat/clear")
  public ResponseEntity<Void> restartChat(@AuthenticationPrincipal String userId) {
    aiChatService.clearSession(userId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/insights/generate")
  public ResponseEntity<AiInsightDto> generateInsights(@AuthenticationPrincipal String userId) {
    return ResponseEntity.ok(aiService.generateInsights(userId));
  }

}

package com.example.et.module.ai.chat;

import com.example.et.module.account.AccountService;
import com.example.et.module.ai.dto.ChatMessageRequest;
import com.example.et.module.ai.dto.ChatReplyResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.*;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {
  private static final String CHAT_KEY_PREFIX = "chat:history:";
  private static final Duration CHAT_SESSION_TTL = Duration.ofDays(7);

  private final AccountService accountService;
  private final ChatClient chatClient;
  private final RedisTemplate<String, StoredChatMessage> redisTemplate;

  @Override
  public ChatReplyResponse chat(String userId, ChatMessageRequest request) {
    if (request == null || request.message() == null || request.message().isBlank()) {
      throw new IllegalArgumentException("Chat message must not be blank");
    }

    final var sessionKey = CHAT_KEY_PREFIX + userId;
    final var history = loadChatHistory(sessionKey);

    final var accounts = accountService.getUserAccounts(userId);

    final var accountsSummary = accounts.stream()
        .map(a -> String.format("- Account: %s (%s), Balance: %.2f",
            a.bank() != null && a.bank().name() != null ? a.bank().name() + " *" + a.lastFourDigits() : a.lastFourDigits(),
            a.accountType(),
            a.balance() != null ? a.balance() : 0.0f))
        .collect(Collectors.joining("\n"));

    final var systemPrompt = """
        You are a helpful and concise personal finance assistant.
        Today's date is %s.
        
        User Accounts:
        %s
        
        When the user asks questions about their expenses, spending, or category breakdowns, ALWAYS use the getCategorySpendingSummary tool to fetch accurate real-time data.
        Answer clearly and concisely based on the tool results.
        """.formatted(LocalDate.now(), accountsSummary);

    final var userMessage = new UserMessage(request.message());
    final var promptMessages = new ArrayList<>(history);
    promptMessages.add(userMessage);

    final var promptSpec = chatClient.prompt()
        .system(systemPrompt)
        .toolContext(Map.of("userId", userId))
        .messages(promptMessages);

    final var reply = promptSpec.call().content();
    final var safeReply = reply != null ? reply : "";

    saveMessages(sessionKey, List.of(
        new StoredChatMessage(MessageType.USER, request.message()),
        new StoredChatMessage(MessageType.ASSISTANT, safeReply)
    ));

    return new ChatReplyResponse(safeReply, userId);
  }

  @Override
  public void clearSession(String userId) {
    final var sessionKey = CHAT_KEY_PREFIX + userId;
    redisTemplate.delete(sessionKey);
    log.info("Cleared chat session for user: {}", userId);
  }

  private List<Message> loadChatHistory(String sessionKey) {
    final var storedChatMessages = redisTemplate.opsForList().range(sessionKey, 0, -1);
    if (storedChatMessages == null || storedChatMessages.isEmpty()) {
      return Collections.emptyList();
    }

    final var messages = new ArrayList<Message>(storedChatMessages.size());
    for (final var chatMessage : storedChatMessages) {
      try {
        switch (chatMessage.type()) {
          case USER -> messages.add(new UserMessage(chatMessage.content()));
          case ASSISTANT -> messages.add(new AssistantMessage(chatMessage.content()));
          case SYSTEM -> messages.add(new SystemMessage(chatMessage.content()));
        }
      } catch (Exception e) {
        log.warn("Failed to deserialize chat message from Redis: {}", chatMessage, e);
      }
    }
    return messages;
  }

  private void saveMessages(String sessionKey, List<StoredChatMessage> newMessages) {
    if (newMessages != null && !newMessages.isEmpty()) {
      redisTemplate.opsForList().rightPushAll(sessionKey, newMessages);
      redisTemplate.expire(sessionKey, CHAT_SESSION_TTL);
    }
  }
}

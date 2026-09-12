package com.example.et.service.ai.chat;

import com.example.et.controller.dto.ai.AiChatRequestDto;
import com.example.et.controller.dto.ai.AiChatResponseDto;
import com.example.et.controller.dto.transaction.TransactionFilterParams;
import com.example.et.service.account.AccountService;
import com.example.et.service.transaction.TransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AiChatServiceImpl implements AiChatService {
  private static final String CHAT_KEY_PREFIX = "chat:history:";
  private static final Duration CHAT_SESSION_TTL = Duration.ofDays(7);

  private final AccountService accountService;
  private final TransactionService transactionService;
  private final ChatClient chatClient;
  private final RedisTemplate<String, StoredChatMessage> redisTemplate;

  @Override
  public AiChatResponseDto chat(String userId, AiChatRequestDto request) {
    if (request == null || request.message() == null || request.message().isBlank()) {
      throw new IllegalArgumentException("Chat message must not be blank");
    }

    final var sessionKey = CHAT_KEY_PREFIX + userId;
    final var history = loadChatHistory(sessionKey);

    final var accounts = accountService.getUserAccountsV2(userId);
    final var transactions = transactionService.getAllTransactions(userId, TransactionFilterParams.empty(), PageRequest.of(0, 100));

    final var accountsSummary = accounts.stream()
        .map(a -> String.format("- Account: %s (%s), Balance: %.2f",
            a.bank() != null && a.bank().name() != null ? a.bank().name() + " *" + a.lastFourDigits() : a.lastFourDigits(),
            a.accountType(),
            a.balance() != null ? a.balance() : 0.0f))
        .collect(Collectors.joining("\n"));

    final var transactionsSummary = transactions.content().stream()
        .map(t -> String.format("- %s: %.2f | %s | Category: %s | Description: %s",
            t.transactionDate(),
            Math.abs(t.amount()),
            t.type(),
            t.category() != null ? t.category() : "Uncategorized",
            t.description()))
        .collect(Collectors.joining("\n"));

    final var systemPrompt = """
        You are a helpful and concise personal finance assistant.
        Today's date is %s.

        User Accounts:
        %s

        Recent Transactions (Up to last 100):
        %s

        Answer the user's financial questions accurately based on this data. Keep responses clear and concise.
        """.formatted(LocalDate.now(), accountsSummary, transactionsSummary);

    final var userMessage = new UserMessage(request.message());
    final var promptMessages = new ArrayList<>(history);
    promptMessages.add(userMessage);

    final var promptSpec = chatClient.prompt()
        .system(systemPrompt)
        .messages(promptMessages);

    final var reply = promptSpec.call().content();
    final var safeReply = reply != null ? reply : "";

    saveMessages(sessionKey, List.of(
        new StoredChatMessage(MessageType.USER, request.message()),
        new StoredChatMessage(MessageType.ASSISTANT, safeReply)
    ));

    return new AiChatResponseDto(safeReply, userId);
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
    final var serializedList = new ArrayList<StoredChatMessage>(newMessages.size());
    for (final var msg : newMessages) {
      try {
        serializedList.add(msg);
      } catch (Exception e) {
        log.error("Failed to serialize chat message for Redis: {}", msg, e);
      }
    }

    if (!serializedList.isEmpty()) {
      redisTemplate.opsForList().rightPushAll(sessionKey, serializedList);
      redisTemplate.expire(sessionKey, CHAT_SESSION_TTL);
    }
  }
}

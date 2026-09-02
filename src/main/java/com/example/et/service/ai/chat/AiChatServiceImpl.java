package com.example.et.service.ai.chat;

import com.example.et.controller.dto.AiChatRequestDto;
import com.example.et.controller.dto.AiChatResponseDto;
import com.example.et.model.core.Account;
import com.example.et.model.core.Transaction;
import com.example.et.repo.AccountRepo;
import com.example.et.repo.TransactionRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.AssistantMessage;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AiChatServiceImpl implements AiChatService {
  private final ChatClient chatClient;
  private final AccountRepo accountRepo;
  private final TransactionRepo transactionRepo;

  private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
  private static final long SESSION_TIMEOUT_SECONDS = 900;

  private record ChatSession(List<Message> history, Instant lastAccessedAt) {
    ChatSession withUpdatedAccess() {
      return new ChatSession(this.history, Instant.now());
    }
  }

  @Override
  public AiChatResponseDto chat(String userId, AiChatRequestDto request) {
    final var userUuid = UUID.fromString(userId);
    final var sessionId = (request.sessionId() != null && !request.sessionId().isBlank())
        ? request.sessionId()
        : UUID.randomUUID().toString();

    final var sessionKey = userId + ":" + sessionId;
    final var session = sessions.compute(sessionKey, (k, existing) -> {
      if (existing == null || isExpired(existing)) {
        return new ChatSession(new ArrayList<>(), Instant.now());
      }
      return existing.withUpdatedAccess();
    });

    final var accounts = accountRepo.findByAppUserId(userUuid);
    final var transactions = transactionRepo.findByAppUserIdOrderByTransactionDateDesc(userUuid, PageRequest.of(0, 100));

    final var accountsSummary = accounts.stream()
        .map(a -> String.format("- Account: %s (%s), Balance: %.2f",
            a.getLastFourDigits(),
            a.getAccountType(),
            a.getBalance()))
        .collect(Collectors.joining("\n"));

    final var transactionsSummary = transactions.stream()
        .map(t -> String.format("- %s: %.2f | %s | Category: %s | Description: %s",
            t.getTransactionDate(),
            Math.abs(t.getAmount()),
            t.getType(),
            t.getTransactionCategory() != null ? t.getTransactionCategory().getName() : "Uncategorized",
            t.getDescription()))
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

    session.history().add(new UserMessage(request.message()));

    final var promptSpec = chatClient.prompt()
        .system(systemPrompt)
        .messages(session.history());

    final var reply = promptSpec.call().content();

    session.history().add(new AssistantMessage(reply != null ? reply : ""));

    return new AiChatResponseDto(reply, sessionId);
  }

  private boolean isExpired(ChatSession session) {
    return session.lastAccessedAt().plusSeconds(SESSION_TIMEOUT_SECONDS).isBefore(Instant.now());
  }

  @Scheduled(fixedRate = 60000)
  public void cleanExpiredSessions() {
    sessions.entrySet().removeIf(entry -> isExpired(entry.getValue()));
  }
}

package com.example.et.service.ai.chat;

import com.example.et.controller.dto.AiChatRequestDto;
import com.example.et.controller.dto.AiChatResponseDto;
import com.example.et.controller.dto.TransactionFilterParams;
import com.example.et.service.account.AccountService;
import com.example.et.service.transaction.TransactionService;

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
public class AiChatServiceImpl implements AiChatService{
  private final AccountService accountService;
  private final TransactionService transactionService;
  private final ChatClient chatClient;

  private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
  private static final long SESSION_TIMEOUT_SECONDS = 900;

  private record ChatSession(List<Message> history, Instant lastAccessedAt) {
    ChatSession withUpdatedAccess() {
      return new ChatSession(this.history, Instant.now());
    }
  }

  private boolean isExpired(ChatSession session) {
    return session.lastAccessedAt().plusSeconds(SESSION_TIMEOUT_SECONDS).isBefore(Instant.now());
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

    final var accounts = accountService.getUserAccounts(userId);
    final var transactions = transactionService.getAllTransactions(userId, TransactionFilterParams.empty(), PageRequest.of(0, 100));

    final var accountsSummary = accounts.stream()
        .map(a -> String.format("- Account: %s (%s), Balance: %.2f",
            a.lastFourDigits(),
            a.accountType(),
            a.balance()))
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

    session.history().add(new UserMessage(request.message()));

    final var promptSpec = chatClient.prompt()
        .system(systemPrompt)
        .messages(session.history());

    final var reply = promptSpec.call().content();

    session.history().add(new AssistantMessage(reply != null ? reply : ""));

    return new AiChatResponseDto(reply, sessionId);
  }

  @Scheduled(fixedRate = 60000)
  public void cleanExpiredSessions() {
    sessions.entrySet().removeIf(entry -> isExpired(entry.getValue()));
  }
}

package com.example.et.service.ai.chat;

import com.example.et.controller.dto.account.AccountDto;
import com.example.et.controller.dto.ai.AiChatRequestDto;
import com.example.et.controller.dto.ai.AiChatResponseDto;
import com.example.et.controller.dto.bank.BankDto;
import com.example.et.controller.dto.transaction.PagedTransactionsDto;
import com.example.et.controller.dto.transaction.TransactionFilterParams;
import com.example.et.model.core.Account;
import com.example.et.service.account.AccountService;
import com.example.et.service.transaction.TransactionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AiChatServiceImplTest {

  @Mock
  private AccountService accountService;

  @Mock
  private TransactionService transactionService;

  @Mock
  private ChatClient chatClient;

  @Mock
  private ChatClient.ChatClientRequestSpec requestSpec;

  @Mock
  private ChatClient.CallResponseSpec callResponseSpec;

  @Mock
  private RedisTemplate<String, StoredChatMessage> redisTemplate;

  @Mock
  private ListOperations<String, StoredChatMessage> listOperations;

  private AiChatServiceImpl aiChatService;

  @BeforeEach
  void setUp() {
    aiChatService = new AiChatServiceImpl(
        accountService,
        transactionService,
        chatClient,
        redisTemplate
    );
  }

  @Test
  void chat_successfulFlow_savesMessagesInRedisWithTtl() {
    final String userId = "user-123";
    final String sessionKey = "chat:history:" + userId;

    when(redisTemplate.opsForList()).thenReturn(listOperations);
    when(listOperations.range(sessionKey, 0, -1)).thenReturn(Collections.emptyList());

    final AccountDto mockAccount = new AccountDto(
        UUID.randomUUID(),
        "4321",
        1500.00f,
        Account.AccountType.SAVINGS,
        true,
        true,
        new BankDto(UUID.randomUUID(), "Chase"),
        true
    );
    when(accountService.getUserAccountsV2(userId)).thenReturn(List.of(mockAccount));
    when(transactionService.getAllTransactions(eq(userId), any(TransactionFilterParams.class), eq(PageRequest.of(0, 100))))
        .thenReturn(new PagedTransactionsDto(Collections.emptyList(), 0, 100, 0L, 1, true));

    when(chatClient.prompt()).thenReturn(requestSpec);
    when(requestSpec.system(anyString())).thenReturn(requestSpec);
    when(requestSpec.messages(anyList())).thenReturn(requestSpec);
    when(requestSpec.call()).thenReturn(callResponseSpec);
    when(callResponseSpec.content()).thenReturn("Here is your finance advice");

    AiChatResponseDto response = aiChatService.chat(userId, new AiChatRequestDto("How much do I have?", null));

    assertNotNull(response);
    assertEquals("Here is your finance advice", response.reply());
    assertEquals(userId, response.sessionId());

    verify(listOperations).rightPushAll(eq(sessionKey), anyList());
    verify(redisTemplate).expire(sessionKey, Duration.ofDays(7));
  }

  @Test
  void chat_blankMessage_throwsIllegalArgumentException() {
    assertThrows(IllegalArgumentException.class, () ->
        aiChatService.chat("user-123", new AiChatRequestDto("   ", null))
    );

    verify(redisTemplate, never()).opsForList();
  }

  @Test
  void clearSession_deletesKeyInRedis() {
    final String userId = "user-123";
    final String sessionKey = "chat:history:" + userId;

    aiChatService.clearSession(userId);

    verify(redisTemplate).delete(sessionKey);
  }
}

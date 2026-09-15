package com.example.et.module.reference.bank;

import com.example.et.core.exception.ApiException;
import com.example.et.core.exception.ErrorCode;
import com.example.et.module.reference.bank.dto.BankDetailsResponse;
import com.example.et.module.reference.bank.internal.BankRepo;
import com.example.et.module.reference.bank.internal.BankServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BankServiceImplTest {

  @Mock
  private BankRepo bankRepo;

  @Mock
  private BankMapper bankMapper;

  @InjectMocks
  private BankServiceImpl bankService;

  @Test
  void getSupportedBanks_ShouldReturnListOfBankDtos_WhenBanksExist() {
    UUID bankId = UUID.randomUUID();
    Bank bank = Bank.builder().id(bankId).name("HDFC Bank").build();
    BankDetailsResponse bankDetailsResponse = new BankDetailsResponse(bankId, "HDFC Bank");

    when(bankRepo.findAll()).thenReturn(List.of(bank));
    when(bankMapper.toDto(bank)).thenReturn(bankDetailsResponse);

    List<BankDetailsResponse> result = bankService.getSupportedBanks();

    assertNotNull(result);
    assertEquals(1, result.size());
    assertEquals("HDFC Bank", result.get(0).name());
    assertEquals(bankId, result.get(0).id());

    verify(bankRepo, times(1)).findAll();
    verify(bankMapper, times(1)).toDto(bank);
  }

  @Test
  void getSupportedBanks_ShouldReturnEmptyList_WhenNoBanksExist() {
    when(bankRepo.findAll()).thenReturn(Collections.emptyList());

    List<BankDetailsResponse> result = bankService.getSupportedBanks();

    assertNotNull(result);
    assertTrue(result.isEmpty());

    verify(bankRepo, times(1)).findAll();
    verifyNoInteractions(bankMapper);
  }

  @Test
  void validateBankIds_ShouldSucceed_WhenAllBankIdsExist() {
    UUID bankId1 = UUID.randomUUID();
    UUID bankId2 = UUID.randomUUID();
    Set<UUID> bankIds = Set.of(bankId1, bankId2);

    when(bankRepo.countByIdIn(bankIds)).thenReturn(2L);

    assertDoesNotThrow(() -> bankService.validateBankIds(bankIds));

    verify(bankRepo, times(1)).countByIdIn(bankIds);
  }

  @Test
  void validateBankIds_ShouldThrowApiException_WhenCountDoesNotMatchSize() {
    UUID bankId1 = UUID.randomUUID();
    UUID bankId2 = UUID.randomUUID();
    Set<UUID> bankIds = Set.of(bankId1, bankId2);

    when(bankRepo.countByIdIn(bankIds)).thenReturn(1L);

    ApiException ex = assertThrows(ApiException.class, () -> bankService.validateBankIds(bankIds));

    assertEquals(ErrorCode.INVALID_BANK_IDS, ex.getErrorCode());
    verify(bankRepo, times(1)).countByIdIn(bankIds);
  }

  @Test
  void validateBankIds_ShouldThrowApiException_WhenCountIsNull() {
    UUID bankId = UUID.randomUUID();
    Set<UUID> bankIds = Set.of(bankId);

    when(bankRepo.countByIdIn(bankIds)).thenReturn(null);

    ApiException ex = assertThrows(ApiException.class, () -> bankService.validateBankIds(bankIds));

    assertEquals(ErrorCode.INVALID_BANK_IDS, ex.getErrorCode());
    verify(bankRepo, times(1)).countByIdIn(bankIds);
  }

  @Test
  void validateBankIds_ShouldThrowApiException_WhenBankIdsNull() {
    ApiException ex = assertThrows(ApiException.class, () -> bankService.validateBankIds(null));

    assertEquals(ErrorCode.INVALID_BANK_IDS, ex.getErrorCode());
    verifyNoInteractions(bankRepo);
  }

  @Test
  void validateBankIds_ShouldThrowApiException_WhenBankIdsEmpty() {
    ApiException ex = assertThrows(ApiException.class, () -> bankService.validateBankIds(Collections.emptySet()));

    assertEquals(ErrorCode.INVALID_BANK_IDS, ex.getErrorCode());
    verifyNoInteractions(bankRepo);
  }
}

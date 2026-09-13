package com.example.et.module.reference.bank.internal;

import com.example.et.core.config.CacheNames;
import com.example.et.core.exception.ApiException;
import com.example.et.core.exception.ErrorCode;
import com.example.et.module.reference.bank.BankMapper;
import com.example.et.module.reference.bank.BankService;
import com.example.et.module.reference.bank.dto.BankDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {
  private final BankRepo bankRepo;
  private final BankMapper bankMapper;

  @Override
  @Cacheable(value = CacheNames.BANKS)
  public List<BankDto> getSupportedBanks() {
    return bankRepo.findAll()
        .stream().map(bankMapper::toDto)
        .collect(Collectors.toList());
  }

  @Override
  public void validateBankIds(Set<UUID> bankIds) {
    if (bankIds == null || bankIds.isEmpty()) {
      throw new ApiException(ErrorCode.INVALID_BANK_IDS);
    }
    Long count = bankRepo.countByIdIn(bankIds);
    if (count == null || count != bankIds.size()) {
      throw new ApiException(ErrorCode.INVALID_BANK_IDS);
    }
  }
}

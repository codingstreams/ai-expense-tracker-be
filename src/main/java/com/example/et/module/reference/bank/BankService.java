package com.example.et.module.reference.bank;

import com.example.et.module.reference.bank.dto.BankDto;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface BankService {
  List<BankDto> getSupportedBanks();

  void validateBankIds(Set<UUID> bankIds);
}

package com.example.et.module.reference.bank;

import com.example.et.module.reference.bank.dto.BankDetailsResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface BankService {
  List<BankDetailsResponse> getSupportedBanks();

  void validateBankIds(Set<UUID> bankIds);
}

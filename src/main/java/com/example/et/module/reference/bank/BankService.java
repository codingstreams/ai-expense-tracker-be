package com.example.et.module.reference.bank;

import com.example.et.module.reference.bank.dto.BankDto;

import java.util.List;

public interface BankService {
  List<BankDto> getSupportedBanks();
}

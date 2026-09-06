package com.example.et.service.bank;

import com.example.et.controller.dto.bank.BankDto;

import java.util.List;

public interface BankService {
  List<BankDto> getSupportedBanks();
}

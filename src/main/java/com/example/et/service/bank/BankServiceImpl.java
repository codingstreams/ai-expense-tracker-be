package com.example.et.service.bank;

import com.example.et.controller.dto.bank.BankDto;
import com.example.et.mapper.BankMapper;
import com.example.et.repo.BankRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BankServiceImpl implements BankService {
  private final BankRepo bankRepo;
  private final BankMapper bankMapper;

  @Override
  @Cacheable(value = "banks")
  public List<BankDto> getSupportedBanks() {
    return bankRepo.findAll()
        .stream().map(bankMapper::toDto)
        .toList();
  }
}

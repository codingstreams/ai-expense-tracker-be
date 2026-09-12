package com.example.et.module.reference.bank.internal;

import com.example.et.core.cache.CacheNames;
import com.example.et.module.reference.bank.BankMapper;
import com.example.et.module.reference.bank.BankService;
import com.example.et.module.reference.bank.dto.BankDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
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
}

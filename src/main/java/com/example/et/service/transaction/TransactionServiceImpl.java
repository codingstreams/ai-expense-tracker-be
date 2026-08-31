package com.example.et.service.transaction;

import com.example.et.controller.dto.PagedTransactionsDto;
import com.example.et.controller.dto.TransactionFilterParams;
import com.example.et.controller.dto.TransactionResponseDto;
import com.example.et.model.core.*;
import com.example.et.repo.TransactionRepo;
import com.example.et.repo.spec.TransactionSpecification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TransactionServiceImpl implements  TransactionService {
  private final TransactionRepo transactionRepo;

  private static TransactionResponseDto toDto(Transaction t) {
    return new TransactionResponseDto(
        t.getId(),
        t.getType(),
        t.getAmount(),
        t.getTransactionDate(),
        t.getDescription(),
        Optional.ofNullable(t.getAccount())
            .map(Account::getBank)
            .map(Bank::getName)
            .orElse("CASH"),
        Optional.ofNullable(t.getPaymentMode())
            .map(PaymentMode::getName)
            .orElse(""),
        Optional.ofNullable(t.getTransactionCategory())
            .map(SystemCategory::getName)
            .orElse("")
    );
  }

  @Override
  public PagedTransactionsDto getAllTransactions(String userId, TransactionFilterParams filterParams, Pageable pageable) {
    final var parsedUserId = UUID.fromString(userId);
    final var spec = TransactionSpecification.withFilters(parsedUserId, filterParams);

    final var page = transactionRepo.findAll(spec, pageable)
        .map(TransactionServiceImpl::toDto);

    return PagedTransactionsDto.from(page);
  }
}

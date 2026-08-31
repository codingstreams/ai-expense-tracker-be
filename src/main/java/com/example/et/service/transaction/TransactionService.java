package com.example.et.service.transaction;

import com.example.et.controller.dto.PagedTransactionsDto;
import com.example.et.controller.dto.TransactionFilterParams;
import org.springframework.data.domain.Pageable;

public interface TransactionService {
  PagedTransactionsDto getAllTransactions(String userId, TransactionFilterParams filterParams, Pageable pageable);
}

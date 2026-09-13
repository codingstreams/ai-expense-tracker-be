package com.example.et.module.transaction.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedTransactionsResponse(
    List<TransactionDetailsResponse> content,
    int pageNumber,
    int pageSize,
    long totalElements,
    int totalPages,
    boolean isLast
) {
  public static PagedTransactionsResponse from(Page<TransactionDetailsResponse> page) {
    return new PagedTransactionsResponse(
        page.getContent(),
        page.getNumber(),
        page.getSize(),
        page.getTotalElements(),
        page.getTotalPages(),
        page.isLast()
    );
  }
}

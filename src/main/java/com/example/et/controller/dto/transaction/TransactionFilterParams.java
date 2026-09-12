package com.example.et.controller.dto.transaction;

import org.jspecify.annotations.NonNull;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.StringJoiner;

public record TransactionFilterParams(
    String type,
    String category,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate startDate,

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    LocalDate endDate,

    Float minAmount,
    Float maxAmount
) {
  public static TransactionFilterParams empty() {
    return new TransactionFilterParams(null, null, null, null, null, null);
  }

  public static TransactionFilterParams dateRange(LocalDate startDate, LocalDate endDate) {
    return new TransactionFilterParams(null, null, startDate, endDate, null, null);
  }

  public String toCacheKey() {
    final var sj = new StringJoiner("&");

    if (type != null) sj.add("type=" + type);
    if (category != null) sj.add("category=" + category);
    if (startDate != null) sj.add("startDate=" + startDate);
    if (endDate != null) sj.add("endDate=" + endDate);
    if (minAmount != null) sj.add("minAmount=" + minAmount);
    if (maxAmount != null) sj.add("maxAmount=" + maxAmount);

    return sj.length() > 0 ? sj.toString() : "all";
  }

  @Override
  public @NonNull String toString() {
    return toCacheKey();
  }
}
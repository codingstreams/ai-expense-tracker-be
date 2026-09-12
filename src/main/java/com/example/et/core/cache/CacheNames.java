package com.example.et.core.cache;

public interface CacheNames {
  // --- User-Scoped Caches ---
  String USER_BANK_ACCOUNTS = "user-bank-accounts";
  String USER_CARDS = "user-cards";
  String USER_TRANSACTIONS = "user-transactions";
  String USER_FINANCIAL_SUMMARY = "user-financial-summary";
  String USER_DETAILS = "app-user-details";

  // --- Static / Reference Data Caches ---
  String BANKS = "banks";
  String PAYMENT_MODES = "payment-modes";
  String SYSTEM_CATEGORIES = "system-categories";
}
package com.example.et.service.dataseeder;

import com.example.et.controller.dto.bank.BankDto;
import com.example.et.model.core.PaymentMode;
import com.example.et.model.core.SystemCategory;
import net.datafaker.Faker;

import java.util.List;

public record SeedReferenceData(
    List<BankDto> banks,
    PaymentMode upiPaymentMode,
    PaymentMode cashPaymentMode,
    PaymentMode debitCardPaymentMode,
    PaymentMode creditCardPaymentMode,
    List<SystemCategory> categories
) {

  public BankDto getRandomBank(Faker faker) {
    if (banks.isEmpty()) return null;
    return banks.get(faker.random().nextInt(banks.size()));
  }

  public BankDto getSecondaryBank(BankDto primaryBank) {
    if (banks.size() <= 1 || primaryBank == null) return primaryBank;
    int primaryIndex = banks.indexOf(primaryBank);
    return banks.get((primaryIndex + 1) % banks.size());
  }

  public SystemCategory getRandomCategory(Faker faker) {
    if (categories.isEmpty()) return null;
    return categories.get(faker.random().nextInt(categories.size()));
  }

  public SystemCategory findCategoryByKeywords(String... keywords) {
    for (String keyword : keywords) {
      for (SystemCategory category : categories) {
        if (category.getName() != null && category.getName().toLowerCase().contains(keyword.toLowerCase())) {
          return category;
        }
      }
    }
    return categories.isEmpty() ? null : categories.get(0);
  }
}

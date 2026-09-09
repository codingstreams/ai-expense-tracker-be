package com.example.et.service.dataseeder;

import java.util.UUID;

public class UserSeedState {
  private final String userId;
  private final UUID savingsAccountId;
  private final UUID cashAccountId;
  private final UUID debitCardId;
  private final UUID creditCardId;

  private float savingsBalance;
  private float cashBalance;
  private float creditBalance;

  public UserSeedState(
      String userId,
      UUID savingsAccountId,
      float initialSavingsBalance,
      UUID cashAccountId,
      float initialCashBalance,
      UUID debitCardId,
      UUID creditCardId,
      float initialCreditLimit
  ) {
    this.userId = userId;
    this.savingsAccountId = savingsAccountId;
    this.savingsBalance = initialSavingsBalance;
    this.cashAccountId = cashAccountId;
    this.cashBalance = initialCashBalance;
    this.debitCardId = debitCardId;
    this.creditCardId = creditCardId;
    this.creditBalance = initialCreditLimit;
  }

  public String getUserId() {
    return userId;
  }

  public UUID getSavingsAccountId() {
    return savingsAccountId;
  }

  public UUID getCashAccountId() {
    return cashAccountId;
  }

  public UUID getDebitCardId() {
    return debitCardId;
  }

  public UUID getCreditCardId() {
    return creditCardId;
  }

  public float getSavingsBalance() {
    return savingsBalance;
  }

  public float getCashBalance() {
    return cashBalance;
  }

  public float getCreditBalance() {
    return creditBalance;
  }

  public void creditSavings(float amount) {
    this.savingsBalance += amount;
  }

  public void transferSavingsToCash(float amount) {
    this.savingsBalance -= amount;
    this.cashBalance += amount;
  }

  public void chargeSavings(float amount) {
    this.savingsBalance -= amount;
  }

  public void chargeCash(float amount) {
    this.cashBalance -= amount;
  }

  public void chargeCredit(float amount) {
    this.creditBalance -= amount;
  }
}

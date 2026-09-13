package com.example.et.module.account;

import com.example.et.core.ActivableEntity;
import com.example.et.core.BaseAudit;
import com.example.et.module.reference.bank.Bank;
import com.example.et.module.user.AppUser;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Account extends BaseAudit implements ActivableEntity, Serializable {
  @Serial
  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Column(name = "last_four_digits", nullable = false)
  private String lastFourDigits;

  @Column(name = "balance", nullable = false)
  private Float balance;

  @Column(name = "account_type", nullable = false)
  @Enumerated(EnumType.STRING)
  private Account.AccountType accountType;

  @Column(name = "is_upi_enabled", nullable = false)
  private boolean upiEnabled;

  @Column(name = "is_net_banking_enabled", nullable = false)
  private boolean netBankingEnabled;

  @OneToOne(fetch = FetchType.EAGER)
  private Bank bank;

  @ManyToOne(fetch = FetchType.LAZY)
  @ToString.Exclude
  @JsonIgnore
  private AppUser appUser;

  @ColumnDefault("true")
  private Boolean isActive;

  public static Account ofId(UUID id) {
    return Account.builder().id(id).build();
  }

  @Override
  public Boolean isActive() {
    return isActive;
  }

  public void debit(Float amount) {
    if (Objects.isNull(amount) || amount <= 0) {
      throw new com.example.et.core.exception.ApiException(com.example.et.core.exception.ErrorCode.INVALID_AMOUNT, "Invalid amount");
    }

    if (this.balance == null || this.balance - amount < 0) {
      throw new com.example.et.core.exception.InsufficientAccountBalanceException(this.id);
    }

    this.balance -= amount;
  }

  public void credit(Float amount) {
    if (Objects.isNull(amount) || amount <= 0) {
      throw new com.example.et.core.exception.ApiException(com.example.et.core.exception.ErrorCode.INVALID_AMOUNT, "Invalid amount");
    }

    if (this.balance == null) {
      this.balance = 0f;
    }
    this.balance += amount;
  }

  public enum AccountType {
    SAVINGS, CREDIT, CASH
  }
}

package com.example.et.module.account;

import com.example.et.core.model.ActivableEntity;
import com.example.et.core.model.BaseAudit;
import com.example.et.module.auth.AppUser;
import com.example.et.module.reference.bank.Bank;
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

  @Override
  public Boolean isActive() {
    return isActive;
  }

  public void debit(Float amount) {
    if (Objects.isNull(amount) || amount <= 0) {
      throw new RuntimeException("Invalid amount");
    }

    if (this.balance - amount < 0) {
      throw new RuntimeException("Insufficient amount to debit the expense transaction.");
    }

    this.balance -= amount;
  }

  public void credit(Float amount) {
    if (Objects.isNull(amount) || amount <= 0) {
      throw new RuntimeException("Invalid amount");
    }

    this.balance += amount;
  }

  public enum AccountType {
    SAVINGS, CREDIT, CASH
  }
}

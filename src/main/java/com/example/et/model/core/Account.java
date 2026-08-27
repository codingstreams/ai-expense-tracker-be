package com.example.et.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Account extends BaseAudit implements ActivableEntity {
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
  @Builder.Default
  private boolean isUpiEnabled = false;

  @Column(name = "is_net_banking_enabled", nullable = false)
  @Builder.Default
  private boolean isNetBankingEnabled = false;

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

  public enum AccountType {
    SAVINGS, CREDIT, CASH
  }
}

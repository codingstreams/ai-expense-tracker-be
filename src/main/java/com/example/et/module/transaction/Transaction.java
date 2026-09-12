package com.example.et.module.transaction;

import com.example.et.core.model.BaseAudit;
import com.example.et.module.account.Account;
import com.example.et.module.auth.AppUser;
import com.example.et.module.reference.category.SystemCategory;
import com.example.et.module.reference.paymentmode.PaymentMode;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Transaction extends BaseAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  private Transaction.TransactionType type;

  private Float amount;

  private LocalDate transactionDate;

  private UUID transferId;

  private String description;

  @ManyToOne(fetch = FetchType.EAGER)
  private SystemCategory transactionCategory;

  @ManyToOne(fetch = FetchType.LAZY)
  private AppUser appUser; // Owner

  @ManyToOne(fetch = FetchType.EAGER)
  private Account account;

  @ManyToOne(fetch = FetchType.EAGER)
  private PaymentMode paymentMode;

  public enum TransactionType {
    EXPENSE, INCOME, TRANSFER
  }
}

package com.example.et.model.core;

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

  @ManyToOne(fetch = FetchType.LAZY)
  private SystemCategory transactionCategory;

  @ManyToOne(fetch = FetchType.LAZY)
  private AppUser appUser; // Owner

  @ManyToOne(fetch = FetchType.LAZY)
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY)
  private PaymentMode paymentMode;

  public enum TransactionType {
    EXPENSE, INCOME, TRANSFER
  }
}

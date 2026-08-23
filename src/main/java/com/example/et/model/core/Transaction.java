package com.example.et.model.core;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Transaction {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  private Transaction.TransactionType type;

  private Double amount;
  private LocalDate transactionDate;
  private String transferId;

  private String description;

  public enum TransactionType {
    EXPENSE, INCOME, TRANSFER
  }
}

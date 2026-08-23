package com.example.et.model.core;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMode {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  private String name;

//  @Enumerated(EnumType.STRING)
//  private TransactionBehavior type;

  public static PaymentMode ofId(Long paymentModeId) {
    return PaymentMode.builder().id(paymentModeId).build();
  }
}
package com.example.et.model.core;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUserConfig extends BaseAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  private LanguagePreference languagePreference;

  private BigDecimal spendLimit;

  @Enumerated(EnumType.STRING)
  private AppUserConfig.Currency currency;

  @OneToOne(fetch = FetchType.LAZY)
  private AppUser appUser;

  @OneToOne(fetch = FetchType.LAZY)
  private PaymentMode paymentMode;

  public enum Currency {
    INR,
  }
}

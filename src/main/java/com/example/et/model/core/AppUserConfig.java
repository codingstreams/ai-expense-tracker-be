package com.example.et.model.core;

import jakarta.persistence.*;
import lombok.*;

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

  private Integer spendLimit;

  @Enumerated(EnumType.STRING)
  private AppUserConfig.Currency currency;

  @OneToOne(fetch = FetchType.LAZY)
  private AppUser appUser;

  @OneToOne(fetch = FetchType.LAZY)
  private PaymentMode paymentMode;

  public enum Currency {
    INR,
  }

  public enum LanguagePreference {
    EN("english"), HI("hindi");

    private String desc;

    LanguagePreference(String desc) {
    }
  }
}

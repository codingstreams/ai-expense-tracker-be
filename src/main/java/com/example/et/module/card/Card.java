package com.example.et.module.card;

import com.example.et.core.BaseAudit;
import com.example.et.module.account.Account;
import com.example.et.module.user.AppUser;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Card extends BaseAudit {
  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @Enumerated(EnumType.STRING)
  private CardType cardType;

  private String lastFourDigits;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "account_id")
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "app_user_id")
  private AppUser appUser;

  public enum CardType {
    CREDIT_CARD, DEBIT_CARD
  }
}

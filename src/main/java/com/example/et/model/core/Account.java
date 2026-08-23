package com.example.et.model.core;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Account extends BaseAudit{
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "last_four_digits",  nullable = false)
    private String lastFourDigits;

    @Column(name = "balance", nullable = false)
    private BigDecimal balance; // Limit in case of credit card amount

    @Column(name = "account_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private Account.AccountType accountType;

    @OneToOne(fetch = FetchType.EAGER)
    private Bank bank;

    @ManyToOne(fetch = FetchType.LAZY) // No cascading required as we don't want to update the parent when performing action on child
    @ToString.Exclude
    @JsonIgnore
    private AppUser appUser;

    public enum AccountType{
        SAVINGS, CREDIT
    }
}
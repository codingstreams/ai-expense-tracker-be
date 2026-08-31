package com.example.et.repo;

import com.example.et.controller.dto.AccountDto;
import com.example.et.model.core.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface AccountRepo extends JpaRepository<Account, UUID> {
  @Query("""
    select a from Account a where a.appUser.id = :userId and a.isActive = true
    """)
  List<Account> findByAppUserId(UUID userId);

  @Query("""
          select new com.example.et.controller.dto.AccountDto(a.id, a.lastFourDigits, a.balance, a.accountType, b, a.isUpiEnabled, a.isNetBankingEnabled)
           from Account a
           join a.appUser u
           left join a.bank b
           where u.id = :userId and a.id = :accountId and a.isActive = true
      """)
  AccountDto findByUserIdAndAccountId(UUID userId, UUID accountId);
}
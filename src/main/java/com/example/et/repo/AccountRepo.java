package com.example.et.repo;

import com.example.et.controller.dto.AccountDto;
import com.example.et.model.core.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccountRepo extends JpaRepository<Account, UUID> {
  @Query("select a from Account a left join AppUser u on a.appUser.id = :userId")
  List<Account> findByAppUserId(UUID userId);

  @Query("""
        select new com.example.et.controller.dto.AccountDto(a.id, a.lastFourDigits, a.balance, a.accountType, b)
         from Account a
         join a.appUser u
         join a.bank b
         where u.id = :userId and a.id = :accountId
    """)
  AccountDto findByUserIdAndAccountId(UUID userId, UUID accountId);

  Optional<Account> findByIdAndAppUserId(UUID id, UUID appUserId);
}

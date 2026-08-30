package com.example.et.repo;

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
}
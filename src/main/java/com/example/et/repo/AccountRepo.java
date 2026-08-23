package com.example.et.repo;

import com.example.et.model.core.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface AccountRepo extends JpaRepository<Account, UUID> {
  @Query("select a from Account a left join AppUser u on a.appUser.id = :userId")
  List<Account> findByAppUserId(UUID userId);
}

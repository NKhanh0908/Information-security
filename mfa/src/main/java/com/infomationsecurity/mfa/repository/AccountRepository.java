package com.infomationsecurity.mfa.repository;

import com.infomationsecurity.mfa.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Integer> {
    // Define custom query methods if needed
    // For example, to find an account by username:
    @Transactional(readOnly = true)
    Optional<Account> findByAccountUsername(String username);

    // You can also define methods for other queries as required
}

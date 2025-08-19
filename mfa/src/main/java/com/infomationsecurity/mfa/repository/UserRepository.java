package com.infomationsecurity.mfa.repository;

import com.infomationsecurity.mfa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Integer> {
    // Define custom query methods if needed
    // For example, to find a user by username:
    // Optional<User> findByUsername(String username);

    // You can also define methods for other queries as required
    @Query(value = """
    SELECT u.* 
    FROM users u
    JOIN account a ON u.user_id = a.user_id
    WHERE a.account_id = :accountId
    """, nativeQuery = true)
    User findByAccountId(@Param("accountId") Integer accountId);

}

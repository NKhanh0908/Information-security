package com.infomationsecurity.mfa.repository;

import com.infomationsecurity.mfa.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Integer> {
    // Define custom query methods if needed
    // For example, to find a user by username:
    // Optional<User> findByUsername(String username);

    // You can also define methods for other queries as required
}

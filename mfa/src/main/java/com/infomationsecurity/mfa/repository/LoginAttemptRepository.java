package com.infomationsecurity.mfa.repository;


import com.infomationsecurity.mfa.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Integer> {
    // Define custom query methods if needed
    // For example, to find login attempts by username or IP address
    // Optional<LoginAttempt> findByUsername(String username);
    // Optional<LoginAttempt> findByIpAddress(String ipAddress);
}

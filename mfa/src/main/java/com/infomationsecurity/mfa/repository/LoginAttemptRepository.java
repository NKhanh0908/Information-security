package com.infomationsecurity.mfa.repository;


import com.infomationsecurity.mfa.entity.LoginAttempt;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LoginAttemptRepository extends JpaRepository<LoginAttempt, Integer> {
    // Define custom query methods if needed
    // For example, to find login attempts by username or IP address
    // Optional<LoginAttempt> findByUsername(String username);
    // Optional<LoginAttempt> findByIpAddress(String ipAddress);

    List<LoginAttempt> findByAccount_AccountId(Integer accountId);

    List<LoginAttempt> findByTrustDevice_DeviceIdAndAccount_AccountId(Integer trustDeviceId, Integer accountId);
}

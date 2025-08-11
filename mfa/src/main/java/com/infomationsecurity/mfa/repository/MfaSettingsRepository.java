package com.infomationsecurity.mfa.repository;

import com.infomationsecurity.mfa.entity.MfaSettings;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MfaSettingsRepository extends JpaRepository<MfaSettings, Integer> {
    // Define custom query methods if needed
    // For example, to find MFA settings by account ID or other criteria
    // Optional<MfaSettings> findByAccountId(Integer accountId);
    // Optional<MfaSettings> findByMfaType(String mfaType);
}

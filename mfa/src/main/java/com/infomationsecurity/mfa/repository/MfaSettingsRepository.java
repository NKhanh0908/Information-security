package com.infomationsecurity.mfa.repository;

import com.infomationsecurity.mfa.entity.MfaSettings;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MfaSettingsRepository extends JpaRepository<MfaSettings, Integer> {
    // Define custom query methods if needed
    // For example, to find MFA settings by account ID or other criteria

    Optional<MfaSettings> findMfaSettingsByAccount_AccountId(Integer accountId);

    // Optional<MfaSettings> findByMfaType(String mfaType);
}

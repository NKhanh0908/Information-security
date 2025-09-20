package com.infomationsecurity.mfa.repository;

import com.infomationsecurity.mfa.entity.MfaSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MfaSettingsRepository extends JpaRepository<MfaSettings, Integer> {
    // Define custom query methods if needed
    // For example, to find MFA settings by account ID or other criteria

    Optional<MfaSettings> findMfaSettingsByAccount_AccountId(Integer accountId);

    @Query(value = """
            SELECT mfs.mfa_totp_secret_key
            FROM account a
            INNER JOIN mfa_settings mfs ON a.account_id = mfs.account_id
            WHERE a.account_id = :account_id
              AND mfs.mfa_enabled = TRUE;""", nativeQuery = true)
    String findTotpSecretKeyByAccount_AccountId(@Param("account_id") Integer accountId);

    // Optional<MfaSettings> findByMfaType(String mfaType);
}

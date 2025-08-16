package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.response.MfaSettingsDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.MfaSettings;
import com.infomationsecurity.mfa.enums.MfaMethod;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.exception.Error;
import com.infomationsecurity.mfa.mapper.MfaSettingsMapper;
import com.infomationsecurity.mfa.repository.MfaSettingsRepository;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.MfaSettingsService;
import com.infomationsecurity.mfa.service.TOTPService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MfaSettingsServiceImpl implements MfaSettingsService {
    private final MfaSettingsRepository mfaSettingsRepository;

    private final MfaSettingsMapper mfaSettingsMapper;

    private final TOTPService totpService;
    private final AccountService accountService;

    public MfaSettingsServiceImpl(MfaSettingsRepository mfaSettingsRepository,
                                  MfaSettingsMapper mfaSettingsMapper,
                                  TOTPService totpService,
                                  @Lazy AccountService accountService) {
        this.mfaSettingsRepository = mfaSettingsRepository;
        this.mfaSettingsMapper = mfaSettingsMapper;
        this.totpService = totpService;
        this.accountService = accountService;
    }

    @Async("mfaTaskExecutor")
    @Override
    public CompletableFuture<MfaSettings> create(MfaSettings mfaSettings) {
        log.info("Creating MFA settings for account ID: {}", mfaSettings.getAccount().getAccountId());

        return CompletableFuture.completedFuture(mfaSettingsRepository.save(mfaSettings));
    }

    @Override
    public MfaSettingsDTO update(MfaSettings mfaSettings) {
        return null;
    }

    @Transactional
    @Override
    public void getMfaSettingsByAccountId(AccountDTO accountDTO) {
        log.info("Retrieving MFA settings for account ID: {}", accountDTO.getAccountId());


        MfaSettings mfaSettings = getMfaSettingsByAccount(accountDTO.getAccountId());
        switch (mfaSettings.getMfaPrimaryMethod()) {
            case MfaMethod.EMAIL:
                log.info("MFA settings for account ID {}: Email method", accountDTO.getAccountId());
                // TODO: Implement logic for email MFA settings
                break;
            case MfaMethod.TOTP:
                log.info("MFA settings for account ID {}: TOTP method", accountDTO.getAccountId());
                // TODO: Implement logic for TOTP MFA settings
                break;
            case MfaMethod.AUTHENTICATOR_APP:
                log.info("MFA settings for account ID {}: Authenticator App method", accountDTO.getAccountId());
                // TODO: Implement logic for Authenticator App MFA settings
                break;
            case MfaMethod.WEBAUTHN:
                log.info("MFA settings for account ID {}: WebAuthn method", accountDTO.getAccountId());
                // TODO: Implement logic for WebAuthn MFA settings
                break;
            case MfaMethod.BACKUP_CODES:
                log.info("MFA settings for account ID {}: Backup Codes method", accountDTO.getAccountId());
                // TODO: Implement logic for Backup Codes MFA settings
                break;
            default:
                log.error("Unsupported MFA type: {}", mfaSettings.getMfaBackupMethod());
                throw new CustomException(Error.MFA_METHOD_NOT_SUPPORTED);
        }
    }

    /**
     * Retrieves MFA settings for a specific account.
     * @param accountId
     * @return
     */
    @Override
    public MfaSettings getMfaSettingsByAccount(Integer accountId) {
        log.info("Get MFA settings for account ID: {}", accountId);
        return mfaSettingsRepository.findMfaSettingsByAccount_AccountId(accountId)
                .orElseThrow(() -> new CustomException(Error.MFA_SETTINGS_NOT_FOUND));
    }

    /**
     * @param secretKey
     */
    @Override
    public void updateSecretKey(String secretKey) {
        log.info("Updating secret key for MFA settings");

        AccountDTO accountDTO = accountService.getAccountAuth();
        MfaSettings mfaSettings = getMfaSettingsByAccount(accountDTO.getAccountId());

        mfaSettings.setMfaTotpSecretKey(secretKey);
        mfaSettingsRepository.save(mfaSettings);
    }
}

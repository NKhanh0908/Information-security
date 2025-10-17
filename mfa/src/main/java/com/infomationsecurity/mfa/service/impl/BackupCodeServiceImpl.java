package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.MfaSettings;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.exception.Error;
import com.infomationsecurity.mfa.repository.MfaSettingsRepository;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.BackupCodeService;
import com.infomationsecurity.mfa.service.MfaSettingsService;
import com.infomationsecurity.mfa.util.BackupCodeGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BackupCodeServiceImpl implements BackupCodeService {
    private final String LOG_PREFIX = "[BackupCodeService]: ";
    private final int CODE_LENGTH = 8;
    private final MfaSettingsRepository repository;
    private final MfaSettingsService mfaSettingsService;

    private final AccountService accountService;

    /**
     * @param backupCode
     * @return
     */
    @Override
    public Boolean invalidateBackupCode(String backupCode) {
        log.info("{} Invalidate backup code for account", LOG_PREFIX);
        AccountDTO accountDTO = accountService.getAccountAuth();

        return verifyBackupCode(backupCode, accountDTO.getAccountId());
    }

    @Override
    public Boolean verifyBackupCode(String backupCode, Integer accountId) {
        MfaSettings mfaSettings = mfaSettingsService.getMfaSettingsByAccount(accountId);
        List<String> backupCodes = mfaSettings.getBackupCodes();
        if (backupCodes.contains(backupCode)) {
            backupCodes.remove(backupCode);
            mfaSettings.setBackupCodes(backupCodes);
            repository.save(mfaSettings);
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     */
    @Override
    public void generateBackupCodes() {
        log.info("{} Generate backup codes for account", LOG_PREFIX);
        AccountDTO accountDTO = accountService.getAccountAuth();

        List<String> backupCodes = BackupCodeGenerator.generateBackupCodes(CODE_LENGTH);

        MfaSettings mfaSettings = mfaSettingsService.getMfaSettingsByAccount(accountDTO.getAccountId());


        mfaSettings.setBackupCodes(backupCodes);
        repository.save(mfaSettings);
    }

    /**
     *
     */
    @Override
    public void deleteBackupCodes() {
        AccountDTO accountDTO = accountService.getAccountAuth();

        MfaSettings mfaSettings = mfaSettingsService.getMfaSettingsByAccount(accountDTO.getAccountId());

        mfaSettings.setBackupCodes(null);
        repository.save(mfaSettings);
    }

    /**
     * @return
     */
    @Override
    public List<String> getBackupCodes() {
        log.info("{} Get backup codes for account", LOG_PREFIX);

        AccountDTO accountDTO = accountService.getAccountAuth();
        MfaSettings mfaSettings = mfaSettingsService.getMfaSettingsByAccount(accountDTO.getAccountId());

        return mfaSettings.getBackupCodes();
    }
}

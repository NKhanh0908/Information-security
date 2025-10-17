package com.infomationsecurity.mfa.service;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface BackupCodeService {
    Boolean invalidateBackupCode(String backupCode);

    Boolean verifyBackupCode(String backupCode, Integer accountId);

    void generateBackupCodes();

    void deleteBackupCodes();

    List<String> getBackupCodes();
}

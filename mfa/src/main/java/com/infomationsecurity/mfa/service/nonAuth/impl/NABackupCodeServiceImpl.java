package com.infomationsecurity.mfa.service.nonAuth.impl;

import com.infomationsecurity.mfa.dto.request.nonAuth.backupCodeDTO.BackupCodeVerificationAuth;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.BackupCodeService;
import com.infomationsecurity.mfa.service.nonAuth.NABackupCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class NABackupCodeServiceImpl implements NABackupCodeService {
    private final String LOG_PREFIX = "[NABackupCodeService]: ";
    private final BackupCodeService backupCodeService;
    private final AccountService accountService;


    /**
     * @param backupCodeVerificationAuth
     * @return
     */
    @Override
    public Boolean invalidateBackupCode(BackupCodeVerificationAuth backupCodeVerificationAuth) {
        log.info("{} Verify backup code auth", LOG_PREFIX);
        Optional<Account> accountChecked = accountService.getAccountByEmail(backupCodeVerificationAuth.getEmail());
        if (accountChecked.isPresent()) {
            return backupCodeService.verifyBackupCode(backupCodeVerificationAuth.getCode(),  accountChecked.get().getAccountId());
        }else{
            return Boolean.FALSE;
        }
    }
}

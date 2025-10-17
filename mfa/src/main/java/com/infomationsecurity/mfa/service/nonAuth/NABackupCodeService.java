package com.infomationsecurity.mfa.service.nonAuth;

import com.infomationsecurity.mfa.dto.request.nonAuth.backupCodeDTO.BackupCodeVerificationAuth;
import org.springframework.stereotype.Service;

@Service
public interface NABackupCodeService {
    Boolean invalidateBackupCode(BackupCodeVerificationAuth backupCodeVerificationAuth);
}

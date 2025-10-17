package com.infomationsecurity.mfa.dto.request.nonAuth.backupCodeDTO;

import lombok.Data;

@Data
public class BackupCodeVerificationAuth {
    private String email;
    private String code;
}

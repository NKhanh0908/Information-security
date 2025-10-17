package com.infomationsecurity.mfa.dto.response.settingDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MfaSettingsDTO {
    private Integer mfaId;
    private Boolean mfaEnabled;
    private String mfaPrimaryMethod;
    private String mfaBackupMethod;
    private String mfaTotpSecretKey;
    private Boolean mfaTotpEnable;
    private Boolean mfaEmailEnabled;
    private Boolean mfaWebauthnEnabled;
    private Boolean mfaAuthenticatorAppEnabled;
    private Boolean mfaRequiredMfaForSensitiveActions;
    private LocalDateTime mfaUpdatedAt;
}

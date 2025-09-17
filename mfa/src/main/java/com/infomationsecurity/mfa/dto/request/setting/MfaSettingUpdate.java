package com.infomationsecurity.mfa.dto.request.setting;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class MfaSettingUpdate {
    private Boolean mfaEnabled;
    private String mfaPrimaryMethod;
    private String mfaBackupMethod;
    private Boolean mfaTotpEnable;
    private Boolean mfaEmailEnabled;
    private Boolean mfaWebauthnEnabled;
    private Boolean mfaAuthenticatorAppEnabled;
    private Boolean mfaRequiredMfaForSensitiveActions;
}

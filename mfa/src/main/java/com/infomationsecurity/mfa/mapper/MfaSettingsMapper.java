package com.infomationsecurity.mfa.mapper;

import com.infomationsecurity.mfa.dto.response.MfaSettingsDTO;
import com.infomationsecurity.mfa.entity.MfaSettings;
import org.springframework.stereotype.Component;

@Component
public class MfaSettingsMapper {
    MfaSettingsDTO entityToDTO(MfaSettings mfaSettings) {
        return MfaSettingsDTO.builder()
                .mfaId(mfaSettings.getMfaId())
                .mfaEnabled(mfaSettings.getMfaEnabled())
                .mfaPrimaryMethod(mfaSettings.getMfaPrimaryMethod().name())
                .mfaBackupMethod(mfaSettings.getMfaBackupMethod() != null ? mfaSettings.getMfaBackupMethod().name() : null)
                .mfaTotpSecretKey(mfaSettings.getMfaTotpSecretKey() != null ? mfaSettings.getMfaTotpSecretKey() : null)
                .mfaTotpEnable(mfaSettings.getMfaTotpEnable())
                .mfaEmailEnabled(mfaSettings.getMfaEmailEnabled())
                .mfaWebauthnEnabled(mfaSettings.getMfaWebauthnEnabled())
                .mfaAuthenticatorAppEnabled(mfaSettings.getMfaAuthenticatorAppEnabled())
                .mfaRequiredMfaForSensitiveActions(mfaSettings.getMfaRequiredMfaForSensitiveActions())
                .mfaUpdatedAt(mfaSettings.getMfaUpdatedAt() != null ? mfaSettings.getMfaUpdatedAt() : null)
                .build();
    }
}

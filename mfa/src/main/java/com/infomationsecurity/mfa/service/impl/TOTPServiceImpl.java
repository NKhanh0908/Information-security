package com.infomationsecurity.mfa.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.infomationsecurity.mfa.dto.request.setting.MfaSettingUpdate;
import com.infomationsecurity.mfa.dto.request.totpDTO.TOTPVerificationDTO;
import com.infomationsecurity.mfa.dto.response.TOTPRegistrationDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.MfaSettings;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.exception.Error;
import com.infomationsecurity.mfa.repository.MfaSettingsRepository;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.MfaSettingsService;
import com.infomationsecurity.mfa.service.TOTPService;
import com.infomationsecurity.mfa.util.QRCodeGenerator;
import com.infomationsecurity.mfa.util.TOTPUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class TOTPServiceImpl implements TOTPService {
    private final TOTPUtil totpUtil;
    private final QRCodeGenerator qrCodeGenerator;

    private final ObjectMapper objectMapper;
    private final AccountService accountService;
    private final MfaSettingsService mfaSettingsService;

    public TOTPServiceImpl(TOTPUtil totpUtil, QRCodeGenerator qrCodeGenerator,
                             MfaSettingsRepository mfaSettingsRepository,
                           ObjectMapper objectMapper,
                           @Lazy AccountService accountService,
                           @Lazy MfaSettingsService mfaSettingsService) {
        this.totpUtil = totpUtil;
        this.qrCodeGenerator = qrCodeGenerator;
        this.objectMapper = objectMapper;
        this.accountService = accountService;
        this.mfaSettingsService = mfaSettingsService;
    }


    @Value("${spring.application.name}")
    private String appName;

    @Value("${app.domain.localhost}")
    private String appDomain;

    /**
     * Registers TOTP for the current account.
     * This method generates a TOTP secret key, creates a QR code URL for the user to scan with an authenticator app,
     * and returns a DTO containing the secret key, QR code URL, and backup codes.
     * @return TOTPRegistrationDTO containing the TOTP registration details.
     */
    @Override
    public TOTPRegistrationDTO registerTOTP() {
        log.info("Registering TOTP for account");
        AccountDTO accountDTO = accountService.getAccountAuth();

        try {
            String secretKey = totpUtil.generateSecretKey();
            String qrCodeUrl = totpUtil.getQRBarcodeURL(
                    accountDTO.getAccountUsername(),
                    appDomain,
                    secretKey
            );

            String qrCodeImage = qrCodeGenerator.generateQRCodeBase64(qrCodeUrl, 200, 200);

            mfaSettingsService.updateSecretKey(secretKey);

            return TOTPRegistrationDTO.builder()
                    .qrCodeUrl(qrCodeUrl)
                    .qrCodeImage("data:image/png;base64," + qrCodeImage)
                    .message("TOTP registration successful. Please scan QR code with Google Authenticator.")
                    .build();

        } catch (Exception e) {
            log.error("Error registering TOTP for user: {}", accountDTO.getAccountUsername(), e);
            throw new CustomException(Error.TOTP_REGISTRATION_FAILED);
        }
    }

    /**
     * @param verificationDTO
     * @return
     */
    @Override
    public Boolean verifyRegisterTOTP(TOTPVerificationDTO verificationDTO) {
        log.info("Verifying TOTP for account");

        Boolean result = verifyTOTP(verificationDTO);

        if (result) {
            MfaSettingUpdate mfaSettingUpdate = new MfaSettingUpdate();
            mfaSettingUpdate.setMfaTotpEnable(true);
            mfaSettingsService.update(mfaSettingUpdate, null);
            return true;
        }else {
            return false;
        }

    }

    /**
     * @param verificationDTO
     * @return
     */
    @Override
    public Boolean verifyTOTP(TOTPVerificationDTO verificationDTO) {
        AccountDTO accountDTO = accountService.getAccountAuth();
        log.info("Verifying TOTP code for account ID: {}", accountDTO.getAccountId());

        String secretKey = verificationDTO.getSecretKey();
        if (secretKey == null || secretKey.trim().isEmpty()) {
            MfaSettings mfaSettings = mfaSettingsService.getMfaSettingsByAccount(accountDTO.getAccountId());
            secretKey = mfaSettings.getMfaTotpSecretKey();
        }

        return verifyTOTP(verificationDTO.getCode(), secretKey);
    }

    /**
     * Verifies the provided TOTP code against the secret key.
     * @param code the TOTP code to verify
     * @param secretKey the secret key associated with the TOTP
     * @return true if the code is valid, false otherwise
     */
    @Override
    public Boolean verifyTOTP(String code, String secretKey) {
        log.debug("Verifying TOTP code with secret key");

        if (code == null || code.trim().isEmpty() ||
                secretKey == null || secretKey.trim().isEmpty()) {
            return false;
        }

        try {
            boolean isValid = totpUtil.verifyCode(secretKey, code.trim(), 1);

            log.info("TOTP verification result: {}", isValid);
            return isValid;

        } catch (Exception e) {
            log.error("Error during TOTP verification: ", e);
            return false;
        }
    }

    /**
     * @param account
     * @param totpCode
     * @return
     */
    @Override
    public Boolean verifyLoginRequest(AccountDTO account, String totpCode) {
        return null;
    }

    /**
     * @param account
     * @return
     */
    @Override
    public String[] generateBackupCodes(AccountDTO account) {
        return new String[0];
    }

    /**
     * @param account
     * @param backupCode
     * @return
     */
    @Override
    public Boolean verifyBackupCode(AccountDTO account, String backupCode) {
        return null;
    }
}

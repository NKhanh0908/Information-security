package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.other.RequestInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.VerifyDeviceWithTOTP;
import com.infomationsecurity.mfa.dto.response.MfaSettingsDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.MfaSettings;
import com.infomationsecurity.mfa.entity.TrustDevice;
import com.infomationsecurity.mfa.enums.MfaMethod;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.exception.Error;
import com.infomationsecurity.mfa.mapper.MfaSettingsMapper;
import com.infomationsecurity.mfa.repository.MfaSettingsRepository;
import com.infomationsecurity.mfa.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class MfaSettingsServiceImpl implements MfaSettingsService {
    private final String LOG_PREFIX = "[MfaSettingsService]: ";

    private final MfaSettingsRepository mfaSettingsRepository;

    private final MfaSettingsMapper mfaSettingsMapper;

    private final TOTPService totpService;
    private final AccountService accountService;
    private final TrustDeviceService trustDeviceService;
    private final AuthenticationService authenticationService;
    private final TokenService tokenService;
    private final ActivityLogService activityLogService;

    public MfaSettingsServiceImpl(MfaSettingsRepository mfaSettingsRepository,
                                  MfaSettingsMapper mfaSettingsMapper,
                                  TOTPService totpService,
                                  @Lazy AccountService accountService,
                                  TrustDeviceService trustDeviceService,
                                  @Lazy AuthenticationService authenticationService,
                                  @Lazy TokenService tokenService,
                                  ActivityLogService activityLogService) {
        this.mfaSettingsRepository = mfaSettingsRepository;
        this.mfaSettingsMapper = mfaSettingsMapper;
        this.totpService = totpService;
        this.accountService = accountService;
        this.trustDeviceService = trustDeviceService;
        this.authenticationService = authenticationService;
        this.tokenService = tokenService;
        this.activityLogService = activityLogService;
    }

    @Async("mfaTaskExecutor")
    @Override
    public CompletableFuture<MfaSettings> create(MfaSettings mfaSettings) {
        log.info("{} Creating MFA settings for account ID: {}", LOG_PREFIX, mfaSettings.getAccount().getAccountId());

        return CompletableFuture.completedFuture(mfaSettingsRepository.save(mfaSettings));
    }

    @Override
    public MfaSettingsDTO update(MfaSettings mfaSettings) {
        return null;
    }

    @Transactional
    @Override
    public MfaSettingsDTO getMfaSettingsByAccountId(Integer accountId) {
        log.info("{} Get MFA settings for account ID: {}", LOG_PREFIX, accountId);

        MfaSettings mfaSettings = getMfaSettingsByAccount(accountId);
        switch (mfaSettings.getMfaPrimaryMethod()) {
            case MfaMethod.EMAIL:
                log.info("MFA settings for account ID {}: Email method", accountId);
                // TODO: Implement logic for email MFA settings
                break;
            case MfaMethod.TOTP:
                log.info("MFA settings for account ID {}: TOTP method", accountId);
                // TODO: Implement logic for TOTP MFA settings
                break;
            case MfaMethod.AUTHENTICATOR_APP:
                log.info("MFA settings for account ID {}: Authenticator App method", accountId);
                // TODO: Implement logic for Authenticator App MFA settings
                break;
            case MfaMethod.WEBAUTHN:
                log.info("MFA settings for account ID {}: WebAuthn method", accountId);
                // TODO: Implement logic for WebAuthn MFA settings
                break;
            case MfaMethod.BACKUP_CODES:
                log.info("MFA settings for account ID {}: Backup Codes method", accountId);
                // TODO: Implement logic for Backup Codes MFA settings
                break;
            default:
                log.error("Unsupported MFA type: {}", mfaSettings.getMfaBackupMethod());
                throw new CustomException(Error.MFA_METHOD_NOT_SUPPORTED);
        }

        return mfaSettingsMapper.entityToDTO(mfaSettings);
    }

    /**
     * Retrieves MFA settings for a specific account.
     * @param accountId
     * @return
     */
    @Override
    public MfaSettings getMfaSettingsByAccount(Integer accountId) {
    log.info("{} Retrieving MFA settings for account ID: {}", LOG_PREFIX, accountId);
        return mfaSettingsRepository.findMfaSettingsByAccount_AccountId(accountId)
                .orElseThrow(() -> new CustomException(Error.MFA_SETTINGS_NOT_FOUND));
    }

    /**
     * @param verifyDeviceWithTOTP
     * @return
     */
    @Override
    public AuthenticationDTO verifyLoginWithTOTP(VerifyDeviceWithTOTP verifyDeviceWithTOTP) {
        log.info("{} Verifying login with TOTP for user", LOG_PREFIX);

        Account account = accountService.getAccountByUsername(verifyDeviceWithTOTP.getUsername());

        String totpCode = getTotpSecretKey(account.getAccountId());

        Boolean isValid = totpService.verifyTOTP(verifyDeviceWithTOTP.getTotpVerificationDTO().getCode(), totpCode);

        if (isValid) {
            log.info("{} TOTP verification successful for user: {}", LOG_PREFIX, account.getAccountUsername());

            TrustDevice trustDevice = trustDeviceService.getTrustDeviceById(verifyDeviceWithTOTP.getDeviceId());

            activityLogService.createActivityLog(
                    account.getAccountId(),
                    trustDevice,
                    "You have successfully logged in using TOTP verification for new device."
            );

            trustDeviceService.updateStatus(trustDevice, true, true);

            return tokenService.generateTokens(account);
        } else {
            log.warn("{} TOTP verification failed for user: {}", LOG_PREFIX, account.getAccountUsername());
            return createMfaRequiredResponse(
                    account.getAccountUsername(),
                    verifyDeviceWithTOTP.getDeviceId(),
                    "Invalid TOTP code. Please try again."
            );
        }
    }

    /**
     * @param secretKey
     */
    @Override
    public void updateSecretKey(String secretKey) {
        log.info("{} Updating TOTP secret key for account", LOG_PREFIX);

        AccountDTO accountDTO = accountService.getAccountAuth();
        MfaSettings mfaSettings = getMfaSettingsByAccount(accountDTO.getAccountId());

        mfaSettings.setMfaTotpSecretKey(secretKey);
        mfaSettingsRepository.save(mfaSettings);
    }

    /**
     * @param accountId
     * @return
     */
    @Override
    public String getTotpSecretKey(Integer accountId) {
        return mfaSettingsRepository.findTotpSecretKeyByAccount_AccountId(accountId);
    }


    @Override
    public RequestInfo extractRequestInfo() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = requestAttributes != null ? requestAttributes.getRequest().getRemoteAddr() : "unknown";
        String userAgent = requestAttributes != null ? requestAttributes.getRequest().getHeader("User-Agent") : "unknown";
        return RequestInfo.builder()
                .ip(ip)
                .userAgent(userAgent)
                .build();
    }

    private AuthenticationDTO createMfaRequiredResponse(String username,Integer deviceId, String message) {
        return AuthenticationDTO.builder()
                .token(null)
                .refreshToken(null)
                .mfaRequired(true)
                .deviceId(deviceId)
                .message(message)
                .username(username)
                .build();
    }

}

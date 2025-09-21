package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.other.GitHubUserInfo;
import com.infomationsecurity.mfa.dto.other.RequestInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.MfaSettings;
import com.infomationsecurity.mfa.entity.TrustDevice;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.exception.Error;
import com.infomationsecurity.mfa.service.*;
import com.infomationsecurity.mfa.util.GithubUtils;
import com.infomationsecurity.mfa.util.LoginAttemptChecked;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Slf4j
public class AuthenticationServiceImpl implements AuthenticationService {
    private final String LOG_PREFIX = "[AuthenticationService]: ";

    private final PasswordEncoder passwordEncoder;

    private final LoginAttemptChecked loginAttemptChecked;
    private final AccountService accountService;
    private final LoginAttemptService loginAttemptService;
    private final TrustDeviceService trustDeviceService;
    private final MfaSettingsService mfaSettingsService;
    private final TokenService tokenService;
    private final GithubUtils githubUtils;

    public AuthenticationServiceImpl(@Lazy AccountService accountService,
                                      PasswordEncoder passwordEncoder,
                                      LoginAttemptChecked loginAttemptChecked,
                                      LoginAttemptService loginAttemptService,
                                      TrustDeviceService trustDeviceService,
                                      MfaSettingsService mfaSettingsService,
                                      TokenService tokenService,
                                      GithubUtils githubUtils) {
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.loginAttemptChecked = loginAttemptChecked;
        this.loginAttemptService = loginAttemptService;
        this.trustDeviceService = trustDeviceService;
        this.mfaSettingsService = mfaSettingsService;
        this.tokenService = tokenService;
        this.githubUtils = githubUtils;
    }

    /**
     * @param formLoginDTO
     * @return
     */
    @Override
    public AuthenticationDTO signIn(FormLoginDTO formLoginDTO) {
        try {
            String username = formLoginDTO.getUsername().trim().toLowerCase();
            log.info("{} Attempting to sign in user: {}", LOG_PREFIX, username);

            Account account = accountService.getAccountByUsername(username);
            RequestInfo requestInfo = mfaSettingsService.extractRequestInfo();
            String lockKey = username + ":" + requestInfo.getIp();

            validateAccountLockStatus(account, requestInfo, lockKey);
            validatePassword(formLoginDTO.getPassword(), account, requestInfo, lockKey);

            return processSuccessfulLogin(account, requestInfo, username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param rawPassword
     * @return
     */
    @Override
    public Boolean verifyPassword(String rawPassword) {
        log.info("{} Verifying password for authenticated user", LOG_PREFIX);
        AccountDTO accountDTO = accountService.getAccountAuth();

        return passwordEncoder.matches(rawPassword, accountService.getAccountByUsername(accountDTO.getAccountUsername()).getPassword());
    }

    @Override
    public AuthenticationDTO authWithGitHub(String authorizationCode) {
        try {
            log.info("{} Starting GitHub OAuth2 sign in process", LOG_PREFIX);

            RequestInfo requestInfo = mfaSettingsService.extractRequestInfo();
            GitHubUserInfo githubUserInfo = getGitHubUserInfo(authorizationCode);

            Optional<Account> existingAccount = accountService.getAccountByEmail(githubUserInfo.getEmail());

            Account account = existingAccount.orElseGet(() -> accountService.createGitHubAccount(githubUserInfo));

            if (!account.isAccountNonLocked()) {
                log.warn("{} Account is locked for email: {}", LOG_PREFIX, account.getAccountEmail());
                throw new CustomException(Error.ACCOUNT_LOCKED);
            }

            return processSuccessfulLogin(account, requestInfo, account.getAccountUsername());

        } catch (Exception e) {
            log.error("{} Error during GitHub sign in: ", LOG_PREFIX, e);
            throw new RuntimeException("GitHub sign in failed", e);
        }
    }

    private GitHubUserInfo getGitHubUserInfo(String authorizationCode) {
        String accessToken = githubUtils.exchangeGithubCodeForToken(authorizationCode);
        return githubUtils.getGithubUserInfo(accessToken);
    }


    @Override
    public AuthenticationDTO processSuccessfulLogin(Account account, RequestInfo requestInfo, String username) {
        try {
            log.info("{} Processing successful login for user: {}", LOG_PREFIX, username);

            loginAttemptChecked.loginSucceeded(username + ":" + requestInfo.getIp());
            accountService.updateLastLoginTime(account);

            TrustDevice trustDevice = trustDeviceService.createOrGetTrustDevice(account, requestInfo);

            // Check if MFA is required for this device
            if (!requiresMfaVerification(account, trustDevice)) {
                log.info("{} MFA verification required for device: {} (IP: {})",
                        LOG_PREFIX, trustDevice.getDeviceName(), trustDevice.getDeviceIpAddress());

                // Save login attempt as pending MFA verification
                loginAttemptService.saveLoginAttemptPendingMfa(account, trustDevice, requestInfo.getUserAgent());

                // Return special response indicating MFA is required
                return triggerMfaProcess(account, trustDevice);
            }

            // Normal login flow - device is trusted or first-time verified
            loginAttemptService.saveSuccessfulLoginAttempt(account, trustDevice, requestInfo.getUserAgent());

            return tokenService.generateTokens(account);
        } catch (Exception e) {
            log.error("Error processing successful login: ", e);
            throw new RuntimeException("Login processing failed", e);
        }
    }

    private AuthenticationDTO triggerMfaProcess(Account account, TrustDevice trustDevice) {
        try {
            log.info("{} Triggering MFA process for account: {} on device: {}",
                    LOG_PREFIX, account.getAccountId(), trustDevice.getDeviceName());

            MfaSettings mfaSettings = mfaSettingsService.getMfaSettingsByAccount(account.getAccountId());

            return createMfaRequiredResponse(account, trustDevice, mfaSettings);
        } catch (Exception e) {
            log.error("{} Error triggering MFA process: ", LOG_PREFIX, e);
            throw new RuntimeException("Failed to trigger MFA process", e);
        }
    }

    private AuthenticationDTO createMfaRequiredResponse(Account account,TrustDevice trustDevice, MfaSettings mfaSettings) {
        return AuthenticationDTO.builder()
                .token(null) // No token until MFA is completed
                .refreshToken(null)
                .mfaRequired(true)
                .deviceId(trustDevice.getDeviceId())
                .message("MFA verification required for this device")
                .mfaMethod(mfaSettings.getMfaPrimaryMethod().name())
                .username(account.getAccountUsername())
                .build();
    }

    private boolean requiresMfaVerification(Account account, TrustDevice trustDevice) {
//        if (!isFirstTimeLogin(trustDevice)) {
//            log.info("{} First time login detected for device: {}", LOG_PREFIX, trustDevice.getDeviceName());
//            return false;
//        }

        MfaSettings mfaSettings = mfaSettingsService.getMfaSettingsByAccount(account.getAccountId());

        if (mfaSettings.getMfaEnabled() && !isFirstTimeLogin(trustDevice)) {
            log.info("{} Device is already verified: {}", LOG_PREFIX, trustDevice.getDeviceName());
            return false;
        }

//        if (mfaSettings.getMfaEnabled()) {
//            log.info("{} MFA is disabled for account: {}", LOG_PREFIX, account.getAccountId());
//            return false;
//        }

        log.info("{} MFA verification required for unverified device: {}",
                LOG_PREFIX, trustDevice.getDeviceName());
        return true;
    }

    private boolean isFirstTimeLogin(TrustDevice trustDevice) {
        return trustDevice.getDeviceCreatedAt() != null &&
                trustDevice.getDeviceCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1));
    }

    private void validateAccountLockStatus(Account account, RequestInfo requestInfo, String lockKey) {
        if (!account.isAccountNonLocked()) {
            log.warn("{} Account is locked: {}", LOG_PREFIX, account.getAccountUsername());
            loginAttemptService.saveFailedLoginAttempt(account, null, requestInfo.getUserAgent(), "Account is locked");
            throw new CustomException(Error.ACCOUNT_LOCKED);
        }

        if (loginAttemptChecked.isBlocked(lockKey)) {
            log.warn("{} Account is temporarily locked due to too many failed login attempts: {}", LOG_PREFIX, account.getAccountUsername());
            loginAttemptService.saveFailedLoginAttempt(account, null, requestInfo.getUserAgent(),
                    "Account temporarily locked due to multiple failed attempts");
            throw new CustomException(Error.ACCOUNT_LOCKED_TEMPORARILY);
        }
    }

    private void validatePassword(String password, Account account, RequestInfo requestInfo, String lockKey) {
        if (!passwordEncoder.matches(password, account.getPassword())) {
            handlePasswordValidationFailure(account, requestInfo, lockKey);
        }
    }

    private void handlePasswordValidationFailure(Account account, RequestInfo requestInfo, String lockKey) {
        loginAttemptChecked.loginFailed(lockKey);
        int remaining = loginAttemptChecked.getRemainingAttempts(lockKey);

        log.warn("Login attempt failed for user: {}, remaining attempts: {}", account.getAccountUsername(), remaining);

        String failureReason = remaining <= 0 ? "Account locked due to multiple failed attempts" : "Invalid password";
        loginAttemptService.saveFailedLoginAttempt(account, null, requestInfo.getUserAgent(), failureReason);

        if (remaining <= 0) {
            accountService.lockAccount(account);
            throw new CustomException(com.infomationsecurity.mfa.exception.Error.ACCOUNT_LOCKED_TEMPORARILY);
        } else {
            throw new CustomException(Error.ACCOUNT_INVALID_PASSWORD);
        }
    }
}

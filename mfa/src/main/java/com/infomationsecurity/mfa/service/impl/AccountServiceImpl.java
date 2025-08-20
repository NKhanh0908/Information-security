package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.oath2.GitHubUserInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.RefreshTokenDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.VerifyDeviceWithTOTP;
import com.infomationsecurity.mfa.dto.request.totpDTO.TOTPVerificationDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.*;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.mapper.AccountMapper;
import com.infomationsecurity.mfa.repository.AccountRepository;
import com.infomationsecurity.mfa.service.*;
import com.infomationsecurity.mfa.util.GithubUtils;
import com.infomationsecurity.mfa.util.JwtTokenUtil;
import com.infomationsecurity.mfa.util.LoginAttemptChecked;
import com.infomationsecurity.mfa.util.OtpService;
import com.infomationsecurity.mfa.exception.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final String LOG_PREFIX = "[AccountService]:";

    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;

    private final MfaSettingsService mfaSettingsService;
    private final LoginAttemptService loginAttemptService;
    private final TrustDeviceService trustDeviceService;
    private final LoginAttemptChecked loginAttemptChecked;
    private final OtpService otpService;
    private final GithubUtils githubUtils;
    //private final MailService mailService;
    private final TOTPService totpService;

    @Override
    public AuthenticationDTO signIn(FormLoginDTO formLoginDTO) {
        try {
            String username = formLoginDTO.getUsername().trim().toLowerCase();
            log.info("{} Attempting to sign in user: {}", LOG_PREFIX, username);

            Account account = getAccountByUsername(username);
            RequestInfo requestInfo = extractRequestInfo();
            String lockKey = username + ":" + requestInfo.getIp();

            validateAccountLockStatus(account, requestInfo, lockKey);
            validatePassword(formLoginDTO.getPassword(), account, requestInfo, lockKey);

            return processSuccessfulLogin(account, requestInfo, username);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public AccountDTO signUp(AccountCreateDTO accountCreateDTO) {
        log.info("{} Attempting to sign up user: {}", LOG_PREFIX, accountCreateDTO.getUsername());

        User user = createUser(accountCreateDTO);
        Account account = createAccount(accountCreateDTO, user);
        Account savedAccount = accountRepository.save(account);

        createMfaSettings(savedAccount);

        return accountMapper.entityToDTO(savedAccount);
    }

    @Override
    public AccountDTO signUpWithGoogle() {
        return null;
    }

    @Override
    public AuthenticationDTO authWithGitHub(String authorizationCode) {
        try {
            log.info("{} Starting GitHub OAuth2 sign in process", LOG_PREFIX);

            RequestInfo requestInfo = extractRequestInfo();
            GitHubUserInfo githubUserInfo = getGitHubUserInfo(authorizationCode);

            Optional<Account> existingAccount = accountRepository.findAccountByAccountEmail(githubUserInfo.getEmail());

            Account account = existingAccount.orElseGet(() -> createGitHubAccount(githubUserInfo));

            validateAccountStatus(account);

            return processSuccessfulLogin(account, requestInfo, account.getAccountUsername());

        } catch (Exception e) {
            log.error("{} Error during GitHub sign in: ", LOG_PREFIX, e);
            throw new RuntimeException("GitHub sign in failed", e);
        }
    }

    @Override
    public AccountDTO getAccountAuth() {
        log.info("{} Retrieving authenticated user account information", LOG_PREFIX);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(Error.UNAUTHORIZED);
        }

        Account account = (Account) authentication.getPrincipal();
        return accountMapper.entityToDTO(account);
    }

    @Override
    public AuthenticationDTO refreshToken(RefreshTokenDTO refreshTokenDTO) {
        log.info("{} Refreshing token for user", LOG_PREFIX);
        try {
            String refreshToken = refreshTokenDTO.getRefreshToken();
            validateRefreshToken(refreshToken);

            String username = jwtTokenUtil.extractTokenGetUsername(refreshToken);
            Account account = getAccountByUsername(username);

            return generateTokens(account);
        } catch (Exception e) {
            throw new RuntimeException("Token refresh failed", e);
        }
    }

    /**
     * @param verifyDeviceWithTOTP
     * @return
     */
    @Override
    public AuthenticationDTO verifyLoginWithTOTP(VerifyDeviceWithTOTP verifyDeviceWithTOTP) {
        log.info("{} Verifying login with TOTP for user", LOG_PREFIX);



        return null;
    }


    // =============== PRIVATE HELPER METHODS ===============
    private boolean usernameExists(String username) {
        return accountRepository.findByAccountUsername(username).isPresent();
    }

    private Account getAccountByUsername(String username) {
        return accountRepository.findByAccountUsername(username)
                .orElseThrow(() -> new CustomException(Error.ACCOUNT_NOT_FOUND));
    }

    private RequestInfo extractRequestInfo() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        String ip = requestAttributes != null ? requestAttributes.getRequest().getRemoteAddr() : "unknown";
        String userAgent = requestAttributes != null ? requestAttributes.getRequest().getHeader("User-Agent") : "unknown";
        return new RequestInfo(ip, userAgent);
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
            lockAccount(account);
            throw new CustomException(Error.ACCOUNT_LOCKED_TEMPORARILY);
        } else {
            throw new CustomException(Error.ACCOUNT_INVALID_PASSWORD);
        }
    }

    private void lockAccount(Account account) {
        account.setAccountIsLocked(true);
        account.setAccountLockedTime(LocalDateTime.now());
        accountRepository.save(account);
    }

    private AuthenticationDTO processSuccessfulLogin(Account account, RequestInfo requestInfo, String username) {
        try {
            log.info("{} Processing successful login for user: {}", LOG_PREFIX, username);

            loginAttemptChecked.loginSucceeded(username + ":" + requestInfo.getIp());
            updateLastLoginTime(account);

            TrustDevice trustDevice = createOrGetTrustDevice(account, requestInfo);

            // Check if MFA is required for this device
            if (!requiresMfaVerification(account, trustDevice)) {
                log.info("{} MFA verification required for device: {} (IP: {})",
                        LOG_PREFIX, trustDevice.getDeviceName(), trustDevice.getDeviceIpAddress());

                // Save login attempt as pending MFA verification
                loginAttemptService.saveLoginAttemptPendingMfa(account, trustDevice, requestInfo.getUserAgent());

                // Trigger MFA process based on user's settings
                triggerMfaProcess(account, trustDevice);

                // Return special response indicating MFA is required
                return createMfaRequiredResponse(trustDevice);
            }

            // Normal login flow - device is trusted or first-time verified
            loginAttemptService.saveSuccessfulLoginAttempt(account, trustDevice, requestInfo.getUserAgent());

            return generateTokens(account);
        } catch (Exception e) {
            log.error("Error processing successful login: ", e);
            throw new RuntimeException("Login processing failed", e);
        }
    }

    private boolean requiresMfaVerification(Account account, TrustDevice trustDevice) {
        // Always allow first login (when device is just created)
        if (isFirstTimeLogin(trustDevice)) {
            log.info("{} First time login detected for device: {}", LOG_PREFIX, trustDevice.getDeviceName());
            return false;
        }

        // Check if device is already verified
        if (trustDevice.getDeviceIsVerified()) {
            log.info("{} Device is already verified: {}", LOG_PREFIX, trustDevice.getDeviceName());
            return false;
        }

        // Check if MFA is enabled for the account
        MfaSettings mfaSettings = mfaSettingsService.getMfaSettingsByAccount(account.getAccountId());
        if (!mfaSettings.getMfaEnabled()) {
            log.info("{} MFA is disabled for account: {}", LOG_PREFIX, account.getAccountId());
            return false;
        }

        log.info("{} MFA verification required for unverified device: {}",
                LOG_PREFIX, trustDevice.getDeviceName());
        return true;
    }

    private boolean isFirstTimeLogin(TrustDevice trustDevice) {
        // Check if this is truly the first login from this device
        // by checking if there are any previous login attempts
        return trustDevice.getDeviceCreatedAt() != null &&
                trustDevice.getDeviceCreatedAt().isAfter(LocalDateTime.now().minusMinutes(1));
    }

    private void updateLastLoginTime(Account account) {
        account.setAccountLastLogin(LocalDateTime.now());
        accountRepository.save(account);
    }

    private TrustDevice createOrGetTrustDevice(Account account, RequestInfo requestInfo) {
        return trustDeviceService.create(account, requestInfo.getIp(), requestInfo.getUserAgent());
    }

    private void handleMfaIfRequired(Account account, TrustDevice trustDevice) {
        if (!trustDevice.getDeviceIsVerified()) {
            AccountDTO accountDTO = accountMapper.entityToDTO(account);
            mfaSettingsService.getMfaSettingsByAccountId(accountDTO);
        }
    }

    private void triggerMfaProcess(Account account, TrustDevice trustDevice) {
        try {
            log.info("{} Triggering MFA process for account: {} on device: {}",
                    LOG_PREFIX, account.getAccountId(), trustDevice.getDeviceName());

            AccountDTO accountDTO = accountMapper.entityToDTO(account);
            mfaSettingsService.getMfaSettingsByAccountId(accountDTO);

            // TODO: Additional MFA trigger logic can be added here
            // Example: Send email, generate TOTP, etc.

        } catch (Exception e) {
            log.error("{} Error triggering MFA process: ", LOG_PREFIX, e);
            throw new RuntimeException("Failed to trigger MFA process", e);
        }
    }

    private AuthenticationDTO generateTokens(Account account) {
        String jwtToken = jwtTokenUtil.generateToken((UserDetails) account);
        String refreshToken = jwtTokenUtil.generateRefreshToken((UserDetails) account);

        return AuthenticationDTO.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    private AuthenticationDTO createMfaRequiredResponse(TrustDevice trustDevice) {
        return AuthenticationDTO.builder()
                .token(null) // No token until MFA is completed
                .refreshToken(null)
                .mfaRequired(true)
                .deviceId(trustDevice.getDeviceId())
                .message("MFA verification required for this device")
                .build();
    }

    private User createUser(AccountCreateDTO accountCreateDTO) {
        User user = new User();
        user.setUserName(accountCreateDTO.getUsername());
        user.setUserGender(accountCreateDTO.getGender());
        return user;
    }

    private Account createAccount(AccountCreateDTO accountCreateDTO, User user) {
        Account account = accountMapper.createDTOToEntity(accountCreateDTO);
        account.setAccountPassword(passwordEncoder.encode(accountCreateDTO.getPassword()));
        account.setUser(user);
        return account;
    }

    private void createMfaSettings(Account account) {
        MfaSettings mfaSettings = new MfaSettings();
        mfaSettings.setAccount(account);
        mfaSettingsService.create(mfaSettings);
    }

    private GitHubUserInfo getGitHubUserInfo(String authorizationCode) {
        String accessToken = githubUtils.exchangeGithubCodeForToken(authorizationCode);
        return githubUtils.getGithubUserInfo(accessToken);
    }

    private Account createGitHubAccount(GitHubUserInfo githubUserInfo) {
        log.info("{} Creating new account for GitHub user with email: {}", LOG_PREFIX, githubUserInfo.getEmail());

        validateEmailNotExists(githubUserInfo.getEmail());

        User user = createGitHubUser(githubUserInfo);
        Account account = createAccountFromGithubInfo(githubUserInfo, user);
        Account savedAccount = accountRepository.save(account);

        createMfaSettings(savedAccount);

        return savedAccount;
    }

    private void validateEmailNotExists(String email) {
        Optional<Account> existingAccount = accountRepository.findAccountByAccountEmail(email);
        if (existingAccount.isPresent()) {
            log.warn("{} Account with email {} already exists", LOG_PREFIX, email);
            throw new CustomException(Error.ACCOUNT_EMAIL_ALREADY_EXISTS);
        }
    }

    private User createGitHubUser(GitHubUserInfo githubUserInfo) {
        User user = new User();
        user.setUserName(githubUserInfo.getName() != null ? githubUserInfo.getName() : githubUserInfo.getLogin());
        return user;
    }

    private Account createAccountFromGithubInfo(GitHubUserInfo githubUserInfo, User user) {
        Account account = new Account();
        account.setAccountUsername(generateUniqueUsername(githubUserInfo.getEmail()));
        account.setAccountEmail(githubUserInfo.getEmail());
        account.setAccountPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        account.setAccountIsLocked(false);
        account.setUser(user);
        return account;
    }

    private void validateAccountStatus(Account account) {
        if (!account.isAccountNonLocked()) {
            log.warn("{} Account is locked for email: {}", LOG_PREFIX, account.getAccountEmail());
            throw new CustomException(Error.ACCOUNT_LOCKED);
        }
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new CustomException(Error.INVALID_REFRESH_TOKEN);
        }
    }

    private String generateUniqueUsername(String email) {
        String baseUsername = email.split("@")[0];
        String username = baseUsername;
        int counter = 1;

        while (usernameExists(username)) {
            username = baseUsername + counter;
            counter++;
        }

        return username;
    }




    // =============== INNER CLASSES ===============
    private static class RequestInfo {
        private final String ip;
        private final String userAgent;

        public RequestInfo(String ip, String userAgent) {
            this.ip = ip;
            this.userAgent = userAgent;
        }

        public String getIp() { return ip; }
        public String getUserAgent() { return userAgent; }
    }
}

package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.oath2.GitHubUserInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.RefreshTokenDTO;
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
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenUtil jwtTokenUtil;

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;

    private final UserService userService;
    private final MfaSettingsService mfaSettingsService;
    private final LoginAttemptService loginAttemptService;
    private final TrustDeviceService trustDeviceService;
    private final LoginAttemptChecked loginAttemptChecked;
    private final OtpService otpService;
    private final RestTemplate restTemplate;
    private final GithubUtils githubUtils;
    //private final MailService mailService;

    /**
     * Authenticates a user based on the provided credentials and generates JWT tokens.
     *
     * <p>Process:
     * <ol>
     *   <li>Normalize the username (trim and lowercase).</li>
     *   <li>Retrieve the {@link Account} by username.</li>
     *   <li>Validate the provided password against the stored hashed password.</li>
     *   <li>If valid, generate an access token and a refresh token via {@link JwtTokenUtil}.</li>
     * </ol>
     *
     * @param formLoginDTO the login request containing username and password
     * @return an {@link AuthenticationDTO} containing the JWT access token, refresh token, and role name
     * @throws RuntimeException if the username does not exist, the password is invalid,
     *                          or token generation fails
     */
    @Override
    public AuthenticationDTO signIn(FormLoginDTO formLoginDTO) {
        try {
            log.info("Attempting to sign in user: {}", formLoginDTO.getUsername());
            String name = formLoginDTO.getUsername().trim().toLowerCase();

            Account account = accountRepository.findByAccountUsername(name)
                    .orElseThrow(() -> new CustomException(Error.ACCOUNT_NOT_FOUND));

            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String ip = requestAttributes != null ? requestAttributes.getRequest().getRemoteAddr() : "unknown";
            String userAgent = requestAttributes != null ? requestAttributes.getRequest().getHeader("User-Agent") : "unknown";

            String key = name + ":" + ip;

            if(!account.isAccountNonLocked()) {
                log.warn("Account is locked: {}", name);
                loginAttemptService.saveFailedLoginAttempt(account, null, userAgent, "Account is locked");
                throw new CustomException(Error.ACCOUNT_LOCKED);
            }

            if (loginAttemptChecked.isBlocked(key)) {
                log.warn("Account is temporarily locked due to too many failed login attempts: {}", name);
                loginAttemptService.saveFailedLoginAttempt(account, null, userAgent, "Account temporarily locked due to multiple failed attempts");
                throw new CustomException(Error.ACCOUNT_LOCKED_TEMPORARILY);
            }

            log.info("Account: {}", account);

            if (!passwordEncoder.matches(formLoginDTO.getPassword(), account.getPassword())) {
                loginAttemptChecked.loginFailed(key);

                int remaining = loginAttemptChecked.getRemainingAttempts(key);
                log.warn("Login attempt failed for user: {}, remaining attempts: {}", name, remaining);

                String failureReason = remaining <= 0 ? "Account locked due to multiple failed attempts" : "Invalid password";
                loginAttemptService.saveFailedLoginAttempt(account, null, userAgent, failureReason);

                if (remaining <= 0) {
                    account.setAccountIsLocked(true);
                    account.setAccountLockedTime(LocalDateTime.now());
                    accountRepository.save(account);
                    throw new CustomException(Error.ACCOUNT_LOCKED_TEMPORARILY);
                } else {
                    throw new CustomException(Error.ACCOUNT_INVALID_PASSWORD);
                }
            }

            return processSuccessfulLogin(account, ip, userAgent, name);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Creates a new user account in the system.
     *
     * <p>Process:
     * <ol>
     *   <li>Check if the requested username already exists.</li>
     *   <li>Encode the password and populate account entity fields.</li>
     *   <li>Persist the new account via {@link AccountRepository}.</li>
     * </ol>
     *
     * @param accountCreateDTO the DTO containing the new account details, including username,
     *                         plain-text password, employeeId, and roleId
     * @return an {@link AccountDTO} representing the newly created account
     * @throws RuntimeException if the username already exists or referenced employee/role not found
     */
    @Override
    public AccountDTO signUp(AccountCreateDTO accountCreateDTO) {
        log.info("Creating account for username: {}", accountCreateDTO.getUsername());

        User user = new User();
        user.setUserName(accountCreateDTO.getUsername());
        user.setUserGender(accountCreateDTO.getGender());

        Account account = accountMapper.createDTOToEntity(accountCreateDTO);
        account.setAccountPassword(passwordEncoder.encode(accountCreateDTO.getPassword()));
        account.setUser(user);
        Account accountSaved = accountRepository.save(account);

        MfaSettings mfaSettings = new MfaSettings();
        mfaSettings.setAccount(accountSaved);
        mfaSettingsService.create(mfaSettings);

        return accountMapper.entityToDTO(accountSaved);
    }

    /**
     * @return
     */
    @Override
    public AccountDTO signUpWithGoogle() {
        return null;
    }

    /**
     * @return
     */

    /**
     * @param authorizationCode
     * @return
     */
    @Override
    public AuthenticationDTO authWithGitHub(String authorizationCode) {
        try {
            log.info("Starting GitHub OAuth2 sign in process");

            ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            String ip = requestAttributes != null ? requestAttributes.getRequest().getRemoteAddr() : "unknown";
            String userAgent = requestAttributes != null ? requestAttributes.getRequest().getHeader("User-Agent") : "unknown";

            // Exchange authorization code for access token
            String accessToken = githubUtils.exchangeGithubCodeForToken(authorizationCode);

            // Get user info from GitHub
            GitHubUserInfo githubUserInfo = githubUtils.getGithubUserInfo(accessToken);

            // Find existing account
            Optional<Account> result = accountRepository.findAccountByAccountEmail(githubUserInfo.getEmail());
            // If account does not exist, create a new one
            if(result.isPresent()){
                log.info("Found existing account for email: {}", githubUserInfo.getEmail());

                Account account = result.get();

                if (!account.isAccountNonLocked()) {
                    log.warn("GitHub OAuth2 login attempt for locked account: {}", account.getAccountUsername());
                    throw new CustomException(Error.ACCOUNT_LOCKED);
                }

                return processSuccessfulLogin(account, ip, userAgent, account.getAccountUsername());

            }else{
                log.info("No existing account found for email: {}, creating new account", githubUserInfo.getEmail());
                Account account = createAccountForGithub(authorizationCode);

                return processSuccessfulLogin(account, ip, userAgent, account.getAccountUsername());

            }
        } catch (Exception e) {
            log.error("Error during GitHub sign in: ", e);
            throw new RuntimeException("GitHub sign in failed", e);
        }
    }

    private Account createAccountForGithub(String authorizationCode) {
        try {
            log.info("Starting GitHub OAuth2 sign up process");

            GitHubUserInfo githubUserInfo = githubUtils.getGithubUserInfo(
                    githubUtils.exchangeGithubCodeForToken(authorizationCode)
            );

            // Check if user already exists
            Optional<Account> existingAccount = accountRepository.findAccountByAccountEmail(githubUserInfo.getEmail());
            if (existingAccount.isPresent()) {
                log.warn("Account with email {} already exists", githubUserInfo.getEmail());
                throw new CustomException(Error.ACCOUNT_EMAIL_ALREADY_EXISTS);
            }

            // Create new account
            Account account = createAccountFromGithubInfo(githubUserInfo);
            Account savedAccount = accountRepository.save(account);

            // Create MFA settings
            MfaSettings mfaSettings = new MfaSettings();
            mfaSettings.setAccount(savedAccount);
            mfaSettingsService.create(mfaSettings);

            log.info("Successfully created account via GitHub OAuth2: {}", savedAccount.getAccountUsername());
            return savedAccount;

        } catch (Exception e) {
            log.error("Error during GitHub sign up: ", e);
            throw new RuntimeException("GitHub sign up failed", e);
        }
    }


    private AuthenticationDTO processSuccessfulLogin(Account account, String ip, String userAgent, String username) {
        try {
            loginAttemptChecked.loginSucceeded(username);
            account.setAccountLastLogin(LocalDateTime.now());
            AccountDTO accountDTO = accountMapper.entityToDTO(accountRepository.save(account));

            TrustDevice trustDevice = trustDeviceService.create(account, ip, userAgent);
            if (!trustDevice.getDeviceIsVerified()) {
                log.info("Trust device created for account ID: {}", account.getAccountId());
                mfaSettingsService.getMfaSettingsByAccountId(accountDTO);
            }
            loginAttemptService.saveSuccessfulLoginAttempt(account, trustDevice, userAgent);

            String jwtToken = jwtTokenUtil.generateToken((UserDetails) account);
            String refreshToken = jwtTokenUtil.generateRefreshToken((UserDetails) account);

            return AuthenticationDTO.builder()
                    .token(jwtToken)
                    .refreshToken(refreshToken)
                    .build();
        } catch (Exception e) {
            log.error("Error processing successful login: ", e);
            throw new RuntimeException("Login processing failed", e);
        }
    }

    /**
     * Retrieves the currently authenticated user's account information.
     *
     * @return the {@link Account} of the currently authenticated user
     */
    @Override
    public AccountDTO getAccountAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new CustomException(Error.UNAUTHORIZED);
        }

        Account account = (Account) authentication.getPrincipal();

        log.info("User principal: {}", account);

        return accountMapper.entityToDTO(account);
    }

    /**
     * @param refreshTokenDTO
     * @return
     */
    @Override
    public AuthenticationDTO refreshToken(RefreshTokenDTO refreshTokenDTO) {
        log.info("Refreshing token for user");
        try {
            String refreshToken = refreshTokenDTO.getRefreshToken();
            if (!jwtTokenUtil.isTokenExpired(refreshToken)) {
                throw new CustomException(Error.INVALID_REFRESH_TOKEN);
            }

            String username = jwtTokenUtil.extractTokenGetUsername(refreshToken);
            Account account = accountRepository.findByAccountUsername(username)
                    .orElseThrow(() -> new CustomException(Error.ACCOUNT_NOT_FOUND));

            String jwtToken = jwtTokenUtil.generateToken((UserDetails) account);
            String newRefreshToken = jwtTokenUtil.generateRefreshToken((UserDetails) account);

            return AuthenticationDTO.builder()
                    .token(jwtToken)
                    .refreshToken(newRefreshToken)
                    .build();
        } catch (Exception e) {
            log.error("Error processing successful login: ", e);
            throw new RuntimeException("Login processing failed", e);
        }
    }

    /**
     * Checks if a username already exists in the system.
     *
     * @param username the username to check
     * @return true if the username exists, false otherwise
     */
    private boolean usernameExists(String username) {
        return accountRepository.findByAccountUsername(username).isPresent();
    }

    private Account createAccountFromGithubInfo(GitHubUserInfo githubUserInfo) {
        User user = new User();
        user.setUserName(githubUserInfo.getName() != null ? githubUserInfo.getName() : githubUserInfo.getLogin());

        Account account = new Account();
        account.setAccountUsername(generateUniqueUsername(githubUserInfo.getEmail()));
        account.setAccountEmail(githubUserInfo.getEmail());
        account.setAccountPassword(passwordEncoder.encode(UUID.randomUUID().toString())); // Random password for OAuth users
        account.setAccountIsLocked(false);
        account.setUser(user);

        return account;
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

}

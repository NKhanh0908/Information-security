package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.*;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.mapper.AccountMapper;
import com.infomationsecurity.mfa.repository.AccountRepository;
import com.infomationsecurity.mfa.service.*;
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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

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
    @Transactional
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

            if (loginAttemptChecked.isBlocked(key)) {
                log.warn("Account is temporarily locked due to too many failed login attempts: {}", name);
                // saveFailedLoginAttempt(account, null, userAgent, "Account temporarily locked due to multiple failed attempts");
                throw new CustomException(Error.ACCOUNT_LOCKED_TEMPORARILY);
            }

            log.info("Account: {}", account);

            if (!passwordEncoder.matches(formLoginDTO.getPassword(), account.getPassword())) {
                loginAttemptChecked.loginFailed(key);

                int remaining = loginAttemptChecked.getRemainingAttempts(key);
                log.warn("Login attempt failed for user: {}, remaining attempts: {}", name, remaining);

//                String failureReason = remaining <= 0 ? "Account locked due to multiple failed attempts" : "Invalid password";
//                saveFailedLoginAttempt(account, null, userAgent, failureReason);

                if (remaining <= 0) {
                    throw new CustomException(Error.ACCOUNT_LOCKED_TEMPORARILY);
                } else {
                    throw new CustomException(Error.ACCOUNT_INVALID_PASSWORD);
                }
            }

            loginAttemptChecked.loginSucceeded(name);

            account.setAccountLastLogin(LocalDateTime.now());
            AccountDTO accountDTO = accountMapper.entityToDTO(accountRepository.save(account));

            TrustDevice trustDevice = trustDeviceService.create(account, ip, userAgent);
            if(!trustDevice.getDeviceIsVerified()) {
                log.info("Trust device created for account ID: {}", account.getAccountId());
                mfaSettingsService.getMfaSettingsByAccountId(accountDTO);
            }
            loginAttemptService.create(account, trustDevice, userAgent);

            try {
                String jwtToken = jwtTokenUtil.generateToken((UserDetails) account);
                String refreshToken = jwtTokenUtil.generateRefreshToken((UserDetails) account);
                return AuthenticationDTO.builder()
                        .token(jwtToken)
                        .refreshToken(refreshToken)
                        .build();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
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
    @Transactional
    @Override
    public AccountDTO signUp(AccountCreateDTO accountCreateDTO) {
        log.info("Creating account for username: {}", accountCreateDTO.getUsername());

        if (usernameExists(accountCreateDTO.getUsername())) {
            throw new CustomException(Error.ACCOUNT_ALREADY_EXISTS);
        }

        User user = new User();
        user.setUserName(accountCreateDTO.getUsername());
        user.setUserGender(accountCreateDTO.getGender());
        User userSaved = userService.create(user);

        Account account = accountMapper.createDTOToEntity(accountCreateDTO);
        account.setAccountPassword(passwordEncoder.encode(accountCreateDTO.getPassword()));
        account.setUser(userSaved);
        Account accountSaved = accountRepository.save(account);

        MfaSettings mfaSettings = new MfaSettings();
        mfaSettings.setAccount(accountSaved);
        mfaSettingsService.create(mfaSettings);

        return accountMapper.entityToDTO(accountSaved);
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

    private boolean usernameExists(String username) {
        return accountRepository.findByAccountUsername(username).isPresent();
    }

}

package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.request.accountDTO.FormLoginDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.LoginAttempt;
import com.infomationsecurity.mfa.entity.MfaSettings;
import com.infomationsecurity.mfa.entity.User;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.mapper.AccountMapper;
import com.infomationsecurity.mfa.repository.AccountRepository;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.LoginAttemptService;
import com.infomationsecurity.mfa.service.MfaSettingsService;
import com.infomationsecurity.mfa.service.UserService;
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
    @Transactional(readOnly = true)
    @Override
    public AuthenticationDTO signIn(FormLoginDTO formLoginDTO) {
        try {
            String name = formLoginDTO.getUsername().trim().toLowerCase();

            Account account = accountRepository.findByAccountUsername(name)
                    .orElseThrow(() -> new CustomException(Error.ACCOUNT_NOT_FOUND));

            String ip = RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes
                    ? ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest().getRemoteAddr()
                    : "unknown";
            String key = name + ":" + ip;

            if (loginAttemptChecked.isBlocked(key)) {
                throw new CustomException(Error.ACCOUNT_LOCKED_TEMPORARILY);
            }

            log.info("Account: {}", account);

            if (!passwordEncoder.matches(formLoginDTO.getPassword(), account.getPassword())) {
                loginAttemptChecked.loginFailed(key);

                int remaining = loginAttemptChecked.getRemainingAttempts(key);
                log.warn("Login attempt failed for user: {}, remaining attempts: {}", name, remaining);
                if (remaining <= 0) {
                    throw new CustomException(Error.ACCOUNT_LOCKED_TEMPORARILY);
                } else {
                    throw new CustomException(Error.ACCOUNT_INVALID_PASSWORD);
                }
            }

            loginAttemptChecked.loginSucceeded(name);

            LoginAttempt loginAttempt = new LoginAttempt();


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

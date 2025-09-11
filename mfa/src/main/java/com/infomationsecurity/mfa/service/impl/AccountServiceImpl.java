package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.other.GitHubUserInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.*;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.mapper.AccountMapper;
import com.infomationsecurity.mfa.repository.AccountRepository;
import com.infomationsecurity.mfa.service.*;
import com.infomationsecurity.mfa.exception.Error;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final String LOG_PREFIX = "[AccountService]:";

    private final PasswordEncoder passwordEncoder;

    private final AccountRepository accountRepository;

    private final AccountMapper accountMapper;

    private final MfaSettingsService mfaSettingsService;
    private final TOTPService totpService;
    private final MailService mailService;

    @Override
    public AccountDTO signUp(AccountCreateDTO accountCreateDTO) {
        log.info("{} Attempting to sign up user: {}", LOG_PREFIX, accountCreateDTO.getUsername());
        if(usernameExists(accountCreateDTO.getUsername())){
            log.warn("{} Username {} already exists", LOG_PREFIX, accountCreateDTO.getUsername());
            throw new CustomException(Error.ACCOUNT_USERNAME_ALREADY_EXISTS);
        }
        if(getAccountByEmail(accountCreateDTO.getEmail()).isPresent()){
            log.warn("{} Email {} already exists", LOG_PREFIX, accountCreateDTO.getEmail());
            throw new CustomException(Error.ACCOUNT_EMAIL_ALREADY_EXISTS);
        }

        User user = createUser(accountCreateDTO);
        Account account = createAccount(accountCreateDTO, user);
        Account savedAccount = accountRepository.save(account);

        mailService.sendVerificationOTPEmail(account.getAccountEmail());

        return accountMapper.entityToDTO(savedAccount);
    }

    @Override
    public AccountDTO signUpWithGoogle() {
        return null;
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
    public Account getAccountByUsername(String username) {
        return accountRepository.findByAccountUsername(username)
                .orElseThrow(() -> new CustomException(Error.ACCOUNT_NOT_FOUND));
    }

    /**
     * @param email
     * @return
     */
    @Override
    public Optional<Account> getAccountByEmail(String email) {
        return accountRepository.findAccountByAccountEmail(email);
    }

    // =============== PRIVATE HELPER METHODS ===============
    private boolean usernameExists(String username) {
        return accountRepository.findByAccountUsername(username).isPresent();
    }

    @Override
    public void lockAccount(Account account) {
        account.setAccountIsLocked(true);
        account.setAccountLockedTime(LocalDateTime.now());
        accountRepository.save(account);
    }

    @Override
    public void updateLastLoginTime(Account account) {
        account.setAccountLastLogin(LocalDateTime.now());
        accountRepository.save(account);
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

    @Override
    public Account createGitHubAccount(GitHubUserInfo githubUserInfo) {
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

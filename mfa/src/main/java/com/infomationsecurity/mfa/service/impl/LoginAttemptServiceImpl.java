package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.response.LoginAttemptDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.LoginAttempt;
import com.infomationsecurity.mfa.entity.TrustDevice;
import com.infomationsecurity.mfa.mapper.LoginAttemptMapper;
import com.infomationsecurity.mfa.repository.LoginAttemptRepository;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.LoginAttemptService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private final LoginAttemptRepository loginAttemptRepository;

    private final LoginAttemptMapper loginAttemptMapper;

    private final AccountService accountService;

    public LoginAttemptServiceImpl(LoginAttemptRepository loginAttemptRepository,
                                   LoginAttemptMapper loginAttemptMapper,
                                   @Lazy AccountService accountService) {
        this.loginAttemptRepository = loginAttemptRepository;
        this.loginAttemptMapper = loginAttemptMapper;
        this.accountService = accountService;
    }

    @Transactional
    @Override
    public LoginAttemptDTO create(LoginAttempt loginAttempt) {

        return loginAttemptMapper.entityToDTO(loginAttemptRepository.save(loginAttempt));
    }

    /**
     * Saves a successful login attempt for the given account and trust device.
     * @param account
     * @param trustDevice
     * @param userAgent
     * @return
     */
    @Async("loginAttemptSuccessTaskExecutor")
    @Transactional
    @Override
    public CompletableFuture<LoginAttemptDTO> saveSuccessfulLoginAttempt(Account account, TrustDevice trustDevice, String userAgent) {
        log.info("Saving successful login attempt for account ID: {}", account.getAccountId());

        LoginAttempt loginAttempt = loginAttemptMapper.createSuccessfulLoginAttempt(userAgent);
        loginAttempt.setAccount(account);
        loginAttempt.setTrustDevice(trustDevice);
        return CompletableFuture.completedFuture(create(loginAttempt));
    }

    /**
     * Saves a failed login attempt for the given account and trust device.
     * @param account
     * @param trustDevice
     * @param userAgent
     * @param failureReason
     * @return
     */
    @Async("loginAttemptFailedTaskExecutor")
    @Transactional
    @Override
    public CompletableFuture<LoginAttemptDTO> saveFailedLoginAttempt(Account account, TrustDevice trustDevice, String userAgent, String failureReason) {
        log.info("Saving failed login attempt for account ID: {}", account.getAccountId());

        LoginAttempt loginAttempt = loginAttemptMapper.createFailedLoginAttempt(userAgent, failureReason);
        loginAttempt.setAccount(account);
        loginAttempt.setTrustDevice(trustDevice);
        return CompletableFuture.completedFuture(create(loginAttempt));
    }

    @Override
    public List<LoginAttemptDTO> getLoginAttemptByAccount() {
        AccountDTO accountDTO = accountService.getAccountAuth();

        log.info("Retrieving login attempts for account ID: {}", accountDTO.getAccountId());
        return loginAttemptRepository.findByAccount_AccountId(accountDTO.getAccountId())
                .stream()
                .map(loginAttemptMapper::entityToDTO)
                .toList();
    }

    /**
     * @param trustDeviceId
     * @return
     */
    @Override
    public List<LoginAttemptDTO> getLoginAttemptByTrustDeviceId(Integer trustDeviceId) {
    log.info("Retrieving login attempts for trust device ID: {}", trustDeviceId);

    AccountDTO accountDTO = accountService.getAccountAuth();

    return loginAttemptRepository.findByTrustDevice_DeviceIdAndAccount_AccountId(trustDeviceId, accountDTO.getAccountId())
            .stream()
            .map(loginAttemptMapper::entityToDTO)
            .toList();
    }
}

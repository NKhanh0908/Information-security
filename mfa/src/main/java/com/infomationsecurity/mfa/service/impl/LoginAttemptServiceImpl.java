package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.request.fiters.LoginAttemptFilter;
import com.infomationsecurity.mfa.dto.response.LoginAttemptDTO;
import com.infomationsecurity.mfa.dto.response.PageDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.LoginAttempt;
import com.infomationsecurity.mfa.entity.TrustDevice;
import com.infomationsecurity.mfa.mapper.LoginAttemptMapper;
import com.infomationsecurity.mfa.repository.LoginAttemptRepository;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.LoginAttemptService;
import com.infomationsecurity.mfa.specification.LoginAttemptSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.JpaSort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private final String LOG_PREFIX = "[LoginAttemptService]: ";

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
        log.info("{} Saving successful login attempt for account ID: {}", LOG_PREFIX, account.getAccountId());

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
        log.info("{} Saving failed login attempt for account ID: {}, failure reason: {}", LOG_PREFIX, account.getAccountId(), failureReason);

        LoginAttempt loginAttempt = loginAttemptMapper.createFailedLoginAttempt(userAgent, failureReason);
        loginAttempt.setAccount(account);
        loginAttempt.setTrustDevice(trustDevice);
        return CompletableFuture.completedFuture(create(loginAttempt));
    }

    @Transactional
    @Override
    public CompletableFuture<LoginAttemptDTO> saveLoginAttemptPendingMfa(Account account, TrustDevice trustDevice, String userAgent) {
        log.info("{} Saving login attempt pending MFA verification for account ID: {}, device: {}",
                LOG_PREFIX, account.getAccountId(), trustDevice.getDeviceName());

        LoginAttempt loginAttempt = loginAttemptMapper.createPendingMfaLoginAttempt(userAgent);
        loginAttempt.setAccount(account);
        loginAttempt.setTrustDevice(trustDevice);
        return CompletableFuture.completedFuture(create(loginAttempt));
    }

    @Transactional
    @Override
    public LoginAttemptDTO completeMfaVerification(Integer attemptId, boolean mfaSuccess, String mfaMethod) {
        log.info("{} Completing MFA verification for attempt ID: {}, success: {}, method: {}",
                LOG_PREFIX, attemptId, mfaSuccess, mfaMethod);

        LoginAttempt loginAttempt = loginAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Login attempt not found"));

        if (mfaSuccess) {
            loginAttempt.setAttemptSuccess(true);
            loginAttempt.setAttemptFailureReason(null);
            log.info("{} MFA verification successful for attempt ID: {}", LOG_PREFIX, attemptId);
        } else {
            loginAttempt.setAttemptSuccess(false);
            loginAttempt.setAttemptFailureReason("MFA verification failed using " + mfaMethod);
            log.warn("{} MFA verification failed for attempt ID: {}", LOG_PREFIX, attemptId);
        }

        return create(loginAttempt);
    }

    @Override
    public List<LoginAttemptDTO> getLoginAttemptByAccount() {
        AccountDTO accountDTO = accountService.getAccountAuth();

        log.info("{} Retrieving login attempts for account ID: {}", LOG_PREFIX, accountDTO.getAccountId());
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
    log.info("{} Retrieving login attempts for trust device ID: {}", LOG_PREFIX, trustDeviceId);

    AccountDTO accountDTO = accountService.getAccountAuth();

    return loginAttemptRepository.findByTrustDevice_DeviceIdAndAccount_AccountId(trustDeviceId, accountDTO.getAccountId())
            .stream()
            .map(loginAttemptMapper::entityToDTO)
            .toList();
    }

    /**
     * @param filter
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageDTO<LoginAttemptDTO> filter(LoginAttemptFilter filter, Integer page, Integer size) {
        log.info("{} Filtering login attempts with filter: {}, page: {}, size: {}", LOG_PREFIX, filter, page, size);

        AccountDTO accountDTO = accountService.getAccountAuth();
        filter.setAccountId(accountDTO.getAccountId());

        Specification<LoginAttempt> specification = LoginAttemptSpecification.filter(filter);

        Sort defaultSort = Sort.by(Sort.Direction.DESC, "attemptCreatedAt");

        Page<LoginAttempt> loginAttemptPage = loginAttemptRepository.findAll(specification, PageRequest.of(page, size, defaultSort));

        return PageDTO.<LoginAttemptDTO>builder()
                .content(loginAttemptPage.getContent().stream()
                        .map(loginAttemptMapper::entityToDTO)
                        .toList())
                .page(page)
                .size(size)
                .totalElements(loginAttemptPage.getTotalElements())
                .totalPages(loginAttemptPage.getTotalPages())
                .build();
    }
}

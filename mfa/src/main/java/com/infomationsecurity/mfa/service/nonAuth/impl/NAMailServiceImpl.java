package com.infomationsecurity.mfa.service.nonAuth.impl;

import com.infomationsecurity.mfa.dto.request.emailOTP.EmailResendOTP;
import com.infomationsecurity.mfa.dto.request.emailOTP.EmailVerificationDTO;
import com.infomationsecurity.mfa.dto.request.nonAuth.accountDTO.FormRequireNonAuth;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.MailService;
import com.infomationsecurity.mfa.service.nonAuth.NAMailService;
import com.infomationsecurity.mfa.util.OtpService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class NAMailServiceImpl implements NAMailService {
    private final String LOG_PREFIX = "[NAMailService]: ";
    private final MailService mailService;
    private final OtpService otpService;
    private final AccountService accountService;

    /**
     * @param formRequireNonAuth
     */
    @Override
    public void sendEmailRequiredForgotPassword(FormRequireNonAuth formRequireNonAuth) {
        log.info("{} Sending email notification verification",  LOG_PREFIX);

        Optional<Account> account = accountService.getAccountByEmail(formRequireNonAuth.getEmail());
        if (account.isPresent()) {
            EmailResendOTP emailResendOTP = new EmailResendOTP();
            emailResendOTP.setEmail(account.get().getAccountEmail());

            mailService.sendVerificationOTPEmail(emailResendOTP);
        }

    }

    /**
     * @param emailVerificationDTO
     * @return
     */
    @Override
    public Boolean verifyEmailRequiredForgotPassword(EmailVerificationDTO emailVerificationDTO) {
        log.info("{} Verifying email verification",  LOG_PREFIX);

        return mailService.verifyEmail(emailVerificationDTO).getSuccess();
    }
}

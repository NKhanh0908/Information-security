package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.other.GitHubUserInfo;
import com.infomationsecurity.mfa.dto.request.accountDTO.AccountCreateDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.entity.Account;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public interface AccountService {

    AccountDTO signUp(AccountCreateDTO accountCreateDTO);

    AccountDTO signUpWithGoogle();

    AccountDTO getAccountAuth();

    Account getAccountByUsername(String username);

    Account createGitHubAccount(GitHubUserInfo githubUserInfo);

    Optional<Account> getAccountByEmail(String email);

    void lockAccount(Account account);

    void updateLastLoginTime(Account account);

    //ForgotPasswordResponseDTO forgotPassword(String email);

    //OtpVerificationResponseDTO verifyOtp(OtpVerificationDTO otpVerificationDTO);

    //String resetPassword(ResetPasswordDTO resetPasswordDTO);

}

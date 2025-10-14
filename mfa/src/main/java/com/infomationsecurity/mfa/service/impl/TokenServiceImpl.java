package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.request.accountDTO.RefreshTokenDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.exception.CustomException;
import com.infomationsecurity.mfa.exception.Error;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.TokenService;
import com.infomationsecurity.mfa.util.JwtTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class TokenServiceImpl implements TokenService {
    private final String LOG_PREFIX = "[TokenService]: ";

    private final JwtTokenUtil jwtTokenUtil;

    private final AccountService accountService;

    public TokenServiceImpl(JwtTokenUtil jwtTokenUtil,
                            @Lazy AccountService accountService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.accountService = accountService;
    }
    /**
     * @param refreshToken
     * @return
     */
    @Override
    public AuthenticationDTO refreshToken(String refreshToken) {
        log.info("{} Refreshing token for user", LOG_PREFIX);
        try {
            validateRefreshToken(refreshToken);

            String username = jwtTokenUtil.extractTokenGetUsername(refreshToken);
            Account account = accountService.getAccountByUsername(username);

            return generateTokens(account);
        } catch (Exception e) {
            throw new RuntimeException("Token refresh failed", e);
        }
    }

    /**
     * @param account
     * @return
     */
    @Override
    public AuthenticationDTO generateTokens(Account account) {
        log.info("{} Generating tokens for account ID: {}", LOG_PREFIX, account.getAccountId());
        String jwtToken = jwtTokenUtil.generateToken((UserDetails) account);
        String refreshToken = jwtTokenUtil.generateRefreshToken((UserDetails) account);

        return AuthenticationDTO.builder()
                .token(jwtToken)
                .refreshToken(refreshToken)
                .build();
    }

    /**
     * @param refreshToken
     * @return
     */
    @Override
    public String extractTokenGetUsername(String refreshToken) {
        return jwtTokenUtil.extractTokenGetUsername(refreshToken);
    }

    private void validateRefreshToken(String refreshToken) {
        if (!jwtTokenUtil.isTokenExpired(refreshToken)) {
            throw new CustomException(Error.INVALID_REFRESH_TOKEN);
        }
    }
}

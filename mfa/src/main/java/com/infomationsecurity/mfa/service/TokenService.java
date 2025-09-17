package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.request.accountDTO.RefreshTokenDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AuthenticationDTO;
import com.infomationsecurity.mfa.entity.Account;
import org.springframework.stereotype.Service;

@Service
public interface TokenService {
    AuthenticationDTO refreshToken(RefreshTokenDTO refreshTokenDTO);

    AuthenticationDTO generateTokens(Account account);

    String extractTokenGetUsername(String refreshToken);

}

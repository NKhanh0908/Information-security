package com.infomationsecurity.mfa.dto.response.accountDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthenticationDTO {
    private String token;
    private String refreshToken;
    private String role;
}

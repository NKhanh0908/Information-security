package com.infomationsecurity.mfa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class AccountDTO {
    private Integer accountId;
    private Integer userId;
    private String accountUsername;
    private String accountPassword;
    private String accountEmail;
    private Boolean accountIsLocked;
    private LocalDateTime accountLockedTime;
    private LocalDateTime accountLastLogin;
    private LocalDateTime accountCreatedAt;
    private LocalDateTime accountUpdatedAt;
}

package com.infomationsecurity.mfa.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class LoginAttemptDTO {
    private Integer attemptId;
    private Integer accountId;
    private String attemptIpAddress;
    private Boolean attemptSuccess;
    private String attemptUserAgent;
    private String attemptFailureReason;
    private String attemptCreatedAt;
    private String trustDeviceName;
    private String trustDeviceIpAddress;
}

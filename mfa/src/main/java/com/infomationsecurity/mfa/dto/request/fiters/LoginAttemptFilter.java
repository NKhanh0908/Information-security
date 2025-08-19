package com.infomationsecurity.mfa.dto.request.fiters;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class LoginAttemptFilter {
    private Integer accountId;
    private Integer trustDeviceId;
    private Boolean attemptSuccess;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
}

package com.infomationsecurity.mfa.dto.request.fiters;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class TrustDeviceFilter {
    private Integer accountId;
    private String deviceName;
    private Boolean deviceIsActive;
    private Boolean deviceIsVerified;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}

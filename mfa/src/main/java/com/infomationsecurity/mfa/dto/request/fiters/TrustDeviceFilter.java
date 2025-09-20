package com.infomationsecurity.mfa.dto.request.fiters;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class TrustDeviceFilter {
    private Integer accountId;
    private String deviceName;
    private Boolean deviceIsActive;
    private Boolean deviceIsVerified;
    private LocalDateTime fromDate;
    private LocalDateTime toDate;
}

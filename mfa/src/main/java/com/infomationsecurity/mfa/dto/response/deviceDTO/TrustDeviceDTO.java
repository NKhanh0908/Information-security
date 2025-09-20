package com.infomationsecurity.mfa.dto.response.deviceDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class TrustDeviceDTO {
    private Integer trustDeviceId;
    private Integer accountId;
    private String trustDeviceName;
    private String deviceIpAddress;
    private String deviceLocation;
    private String deviceUserAgent;
    private Boolean deviceIsActive;
    private Boolean deviceIsVerified;
    private LocalDateTime deviceCreatedAt;
    private LocalDateTime deviceUpdatedAt;
}

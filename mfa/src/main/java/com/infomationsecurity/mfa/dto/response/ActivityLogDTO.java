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
public class ActivityLogDTO {
    private Integer activityLogId;
    private Integer deviceId;
    private String deviceName;
    private String deviceIpAddress;
    private String logAction;
    private LocalDateTime logTimestamp;
}

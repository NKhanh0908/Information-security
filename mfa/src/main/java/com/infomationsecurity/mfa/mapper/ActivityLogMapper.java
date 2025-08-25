package com.infomationsecurity.mfa.mapper;

import com.infomationsecurity.mfa.dto.response.ActivityLogDTO;
import com.infomationsecurity.mfa.entity.ActivityLog;
import com.infomationsecurity.mfa.entity.TrustDevice;
import org.springframework.stereotype.Component;

@Component
public class ActivityLogMapper {

    public ActivityLog toEntity(Integer accountId, TrustDevice trustDevice, String logAction){
        return ActivityLog.builder()
                .accountId(accountId)
                .device(trustDevice)
                .logAction(logAction)
                .build();
    }

    public ActivityLogDTO entityToDTO(ActivityLog activityLog){
        return ActivityLogDTO.builder()
                .activityLogId(activityLog.getActivityLogId())
                .deviceId(activityLog.getDevice().getDeviceId())
                .deviceName(activityLog.getDevice().getDeviceName())
                .deviceIpAddress(activityLog.getDevice().getDeviceIpAddress())
                .logAction(activityLog.getLogAction())
                .logTimestamp(activityLog.getLogTimestamp())
                .build();
    }
}

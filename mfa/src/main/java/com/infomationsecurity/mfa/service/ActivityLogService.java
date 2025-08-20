package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.request.fiters.ActivityLogFilter;
import com.infomationsecurity.mfa.dto.response.ActivityLogDTO;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

@Service
public interface ActivityLogService {
    void createActivityLog(Integer accountId, Integer deviceId, String logAction);

    Page<ActivityLogDTO> getActivityLogs(ActivityLogFilter filter, Integer page, Integer size);
}

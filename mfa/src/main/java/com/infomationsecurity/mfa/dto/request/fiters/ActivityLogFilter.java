package com.infomationsecurity.mfa.dto.request.fiters;

import lombok.Getter;

@Getter
public class ActivityLogFilter {
    private Integer accountId;
    private Integer deviceId;
    private String logAction;
    private String startDate;
    private String endDate;
}

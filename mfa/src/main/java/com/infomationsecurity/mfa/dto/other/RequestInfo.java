package com.infomationsecurity.mfa.dto.other;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RequestInfo {
    private String ip;
    private String userAgent;
}

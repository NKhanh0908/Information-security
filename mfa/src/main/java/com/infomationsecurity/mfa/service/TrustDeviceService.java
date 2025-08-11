package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.response.TrustDeviceDTO;
import com.infomationsecurity.mfa.entity.TrustDevice;
import org.springframework.stereotype.Service;

@Service
public interface TrustDeviceService {
    TrustDeviceDTO create(TrustDevice trustDevice);

    TrustDeviceDTO update(TrustDevice trustDevice);

    TrustDeviceDTO getTrustDeviceByAccountId(Integer accountId);
}

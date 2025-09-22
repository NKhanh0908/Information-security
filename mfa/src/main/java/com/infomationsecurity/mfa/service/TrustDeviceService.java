package com.infomationsecurity.mfa.service;

import com.infomationsecurity.mfa.dto.other.RequestInfo;
import com.infomationsecurity.mfa.dto.request.fiters.TrustDeviceFilter;
import com.infomationsecurity.mfa.dto.response.PageDTO;
import com.infomationsecurity.mfa.dto.response.deviceDTO.TrustDeviceDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.TrustDevice;
import org.springframework.stereotype.Service;

@Service
public interface TrustDeviceService {
    TrustDevice create(Account account, String ip, String userAgent, Boolean isVerify);

    TrustDeviceDTO update(TrustDevice trustDevice);

    TrustDevice getTrustDeviceById(Integer trustDeviceId);

    void updateStatus(TrustDevice trustDevice, Boolean statusVerified, Boolean statusActive);

    void updateDeviceVerify(Account account);

    TrustDevice createOrGetTrustDevice(Account account, RequestInfo requestInfo, Boolean isVerify);

    PageDTO<TrustDeviceDTO> filter(TrustDeviceFilter filter, Integer page, Integer size);

    void deleteTrustDevice(Integer trustDeviceId);
}

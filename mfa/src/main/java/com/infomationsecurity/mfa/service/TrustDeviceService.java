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
    TrustDevice create(Account account, String ip, String userAgent);

    TrustDeviceDTO update(TrustDevice trustDevice);

    TrustDeviceDTO getTrustDeviceByAccount();

    void updateStatus(Integer trustDeviceId, Boolean statusVerified, Boolean statusActive);

    TrustDevice createOrGetTrustDevice(Account account, RequestInfo requestInfo);

    PageDTO<TrustDeviceDTO> filter(TrustDeviceFilter filter, Integer page, Integer size);
}

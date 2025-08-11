package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.response.TrustDeviceDTO;
import com.infomationsecurity.mfa.entity.TrustDevice;
import com.infomationsecurity.mfa.mapper.TrustDeviceMapper;
import com.infomationsecurity.mfa.repository.TrustDeviceRepository;
import com.infomationsecurity.mfa.service.TrustDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Slf4j
@Service
public class TrustDeviceServiceImpl implements TrustDeviceService {
    private final TrustDeviceRepository trustDeviceRepository;

    private final TrustDeviceMapper trustDeviceMapper;

    @Override
    public TrustDevice create(TrustDevice trustDevice) {
        log.info("Creating trust device for account ID: {}", trustDevice.getAccount().getAccountId());

        return trustDeviceRepository.save(trustDevice);
    }

    @Override
    public TrustDeviceDTO update(TrustDevice trustDevice) {
        return null;
    }

    @Override
    public TrustDeviceDTO getTrustDeviceByAccountId(Integer accountId) {
        return null;
    }
}

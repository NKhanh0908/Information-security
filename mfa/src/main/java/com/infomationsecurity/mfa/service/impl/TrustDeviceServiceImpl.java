package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.response.TrustDeviceDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.TrustDevice;
import com.infomationsecurity.mfa.mapper.TrustDeviceMapper;
import com.infomationsecurity.mfa.repository.TrustDeviceRepository;
import com.infomationsecurity.mfa.service.TrustDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Slf4j
@Service
public class TrustDeviceServiceImpl implements TrustDeviceService {
    private final TrustDeviceRepository trustDeviceRepository;

    private final TrustDeviceMapper trustDeviceMapper;

    @Override
    public TrustDevice create(Account account, String ip, String userAgent) {
        log.info("Creating trust device for account ID: {}", account.getAccountId());

        Optional<TrustDevice> existingTrustDevice = trustDeviceRepository.findByDeviceIpAddressAndDeviceNameAndDeviceLocation(
                ip, extractDeviceName(userAgent), getLocationFromIP(ip));

        if (existingTrustDevice.isPresent()) {
            log.info("Trust device already exists for account ID: {}, IP: {}, Device Name: {}, Location: {}",
                    account.getAccountId(), ip, extractDeviceName(userAgent), getLocationFromIP(ip));
            return existingTrustDevice.get();
        }

        TrustDevice trustDevice = trustDeviceMapper.createTrustDevice(ip, extractDeviceName(userAgent), getLocationFromIP(ip));
        trustDevice.setAccount(account);

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

    private String extractDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Device";
        }

        if (userAgent.contains("Mobile") || userAgent.contains("Android")) {
            return "Mobile Device";
        } else if (userAgent.contains("iPhone") || userAgent.contains("iPad")) {
            return "iOS Device";
        } else if (userAgent.contains("Windows")) {
            return "Windows PC";
        } else if (userAgent.contains("Mac")) {
            return "Mac Computer";
        } else if (userAgent.contains("Linux")) {
            return "Linux Computer";
        } else {
            return "Unknown Device";
        }
    }

    private String getLocationFromIP(String ip) {
        // TODO: Tích hợp với service geolocation như MaxMind, IPStack, etc.
        // Hiện tại trả về giá trị mặc định
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || "unknown".equals(ip)) {
            return "Local/Unknown";
        }
        return "Unknown Location"; // Thay thế bằng logic thực tế
    }
}

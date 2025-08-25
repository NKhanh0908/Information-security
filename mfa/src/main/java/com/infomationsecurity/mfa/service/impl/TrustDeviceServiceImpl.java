package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.other.RequestInfo;
import com.infomationsecurity.mfa.dto.request.fiters.TrustDeviceFilter;
import com.infomationsecurity.mfa.dto.response.PageDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
import com.infomationsecurity.mfa.dto.response.deviceDTO.TrustDeviceDTO;
import com.infomationsecurity.mfa.entity.Account;
import com.infomationsecurity.mfa.entity.TrustDevice;
import com.infomationsecurity.mfa.mapper.TrustDeviceMapper;
import com.infomationsecurity.mfa.repository.TrustDeviceRepository;
import com.infomationsecurity.mfa.service.AccountService;
import com.infomationsecurity.mfa.service.TrustDeviceService;
import com.infomationsecurity.mfa.specification.TrustDeviceSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Slf4j
@Service
public class TrustDeviceServiceImpl implements TrustDeviceService {
    private final String LOG_PREFIX = "[TrustDeviceService]: ";
    private final TrustDeviceRepository trustDeviceRepository;
    private final TrustDeviceMapper trustDeviceMapper;
    private final AccountService accountService;

    public TrustDeviceServiceImpl(TrustDeviceRepository trustDeviceRepository,
                                  TrustDeviceMapper trustDeviceMapper,
                                  @Lazy AccountService accountService) {
        this.trustDeviceRepository = trustDeviceRepository;
        this.trustDeviceMapper = trustDeviceMapper;
        this.accountService = accountService;
    }

    @Override
    public TrustDevice create(Account account, String ip, String userAgent) {
        log.info("{} Creating trust device for account ID: {}", LOG_PREFIX, account.getAccountId());

        DeviceInfo deviceInfo = createDeviceInfo(ip, userAgent);

        Optional<TrustDevice> existingDevice = findExistingTrustDevice(deviceInfo, account.getAccountId());
        if (existingDevice.isPresent()) {
            log.info("{} Found existing trust device for IP: {}", LOG_PREFIX, ip);
            return existingDevice.get();
        }

        return createNewTrustDevice(account, deviceInfo);
    }

    @Override
    public TrustDeviceDTO update(TrustDevice trustDevice) {
        // TODO: Implement update logic
        return null;
    }

    @Override
    public TrustDeviceDTO getTrustDeviceByAccount() {
        AccountDTO accountDTO = accountService.getAccountAuth();
        // TODO: Implement get trust device by account logic
        return null;
    }

    /**
     * @param trustDevice
     * @param statusVerified
     * @param statusActive
     */
    @Override
    public void updateStatus(TrustDevice trustDevice, Boolean statusVerified, Boolean statusActive) {
        log.info("{} Updating trust device status for ID: {}, Verified: {}, Active: {}",
                LOG_PREFIX, trustDevice.getDeviceId(), statusVerified, statusActive);

        if (statusVerified != null) {
            trustDevice.setDeviceIsVerified(statusVerified);
        }
        if (statusActive != null) {
            trustDevice.setDeviceIsActive(statusActive);
        }
        trustDeviceRepository.save(trustDevice);
    }

    @Override
    public PageDTO<TrustDeviceDTO> filter(TrustDeviceFilter filter, Integer page, Integer size) {
        log.info("{} Filtering trust devices with filter: {}, page: {}, size: {}", LOG_PREFIX, filter, page, size);

        AccountDTO accountDTO = accountService.getAccountAuth();
        log.info("{} Account DTO: {}", LOG_PREFIX, accountDTO);
        filter.setAccountId(accountDTO.getAccountId());

        Specification<TrustDevice> specification = TrustDeviceSpecification.filter(filter);
        Pageable pageable = PageRequest.of(page, size);
        Page<TrustDevice> trustDevicePage = trustDeviceRepository.findAll(specification, pageable);

        return buildPageDTO(trustDevicePage, page, size);
    }

    @Override
    public TrustDevice createOrGetTrustDevice(Account account, RequestInfo requestInfo) {
        return create(account, requestInfo.getIp(), requestInfo.getUserAgent());
    }

    @Override
    public TrustDevice getTrustDeviceById(Integer trustDeviceId) {
        return trustDeviceRepository.findById(trustDeviceId)
                .orElseThrow(() -> new RuntimeException("TrustDevice not found with ID: " + trustDeviceId));
    }

    // =============== PRIVATE HELPER METHODS ===============

    private DeviceInfo createDeviceInfo(String ip, String userAgent) {
        String deviceName = extractDeviceName(userAgent);
        String location = getLocationFromIP(ip);
        return new DeviceInfo(ip, deviceName, location);
    }

    private Optional<TrustDevice> findExistingTrustDevice(DeviceInfo deviceInfo, Integer accountId) {
        return trustDeviceRepository.findByDeviceIpAddressAndDeviceNameAndDeviceLocation(
                deviceInfo.getIp(),
                deviceInfo.getDeviceName(),
                deviceInfo.getLocation(),
                accountId
        );
    }

    private TrustDevice createNewTrustDevice(Account account, DeviceInfo deviceInfo) {
        log.info("{} Creating new trust device for IP: {}, Device: {}",
                LOG_PREFIX, deviceInfo.getIp(), deviceInfo.getDeviceName());

        TrustDevice trustDevice = trustDeviceMapper.createTrustDevice(
                deviceInfo.getIp(),
                deviceInfo.getDeviceName(),
                deviceInfo.getLocation()
        );
        trustDevice.setAccount(account);

        return trustDeviceRepository.save(trustDevice);
    }

    private PageDTO<TrustDeviceDTO> buildPageDTO(Page<TrustDevice> trustDevicePage, Integer page, Integer size) {
        return PageDTO.<TrustDeviceDTO>builder()
                .content(trustDevicePage.getContent().stream()
                        .map(trustDeviceMapper::entityToDTO)
                        .toList())
                .page(page)
                .size(size)
                .totalElements(trustDevicePage.getTotalElements())
                .totalPages(trustDevicePage.getTotalPages())
                .build();
    }

    private String getIdAddress() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes != null ? requestAttributes.getRequest().getRemoteAddr() : "unknown";
    }

    private String getUserAgent() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        return requestAttributes != null ? requestAttributes.getRequest().getHeader("User-Agent") : "unknown";
    }

    private String extractDeviceName(String userAgent) {
        if (userAgent == null || userAgent.isEmpty()) {
            return "Unknown Device";
        }

        // Use a more structured approach for device detection
        return DeviceDetector.detectDevice(userAgent);
    }

    private String getLocationFromIP(String ip) {
        if (isLocalOrUnknownIP(ip)) {
            return "Local/Unknown";
        }

        // TODO: Integrate with geolocation service like MaxMind, IPStack, etc.
        return "Unknown Location";
    }

    private boolean isLocalOrUnknownIP(String ip) {
        return "127.0.0.1".equals(ip) ||
                "0:0:0:0:0:0:0:1".equals(ip) ||
                "unknown".equals(ip);
    }

    // =============== INNER CLASSES ===============

    private static class DeviceInfo {
        private final String ip;
        private final String deviceName;
        private final String location;

        public DeviceInfo(String ip, String deviceName, String location) {
            this.ip = ip;
            this.deviceName = deviceName;
            this.location = location;
        }

        public String getIp() { return ip; }
        public String getDeviceName() { return deviceName; }
        public String getLocation() { return location; }
    }

    private static class DeviceDetector {
        public static String detectDevice(String userAgent) {
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
    }
}
package com.infomationsecurity.mfa.service.impl;

import com.infomationsecurity.mfa.dto.request.fiters.TrustDeviceFilter;
import com.infomationsecurity.mfa.dto.response.ActivityLogDTO;
import com.infomationsecurity.mfa.dto.response.PageDTO;
import com.infomationsecurity.mfa.dto.response.TrustDeviceDTO;
import com.infomationsecurity.mfa.dto.response.accountDTO.AccountDTO;
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
        log.info("{} Creating trust device for account ID: {}, IP: {}, User Agent: {}", LOG_PREFIX, account.getAccountId(), ip, userAgent);

        Optional<TrustDevice> existingTrustDevice = trustDeviceRepository.findByDeviceIpAddressAndDeviceNameAndDeviceLocation(
                ip, extractDeviceName(userAgent), getLocationFromIP(ip));

        if (existingTrustDevice.isPresent()) {
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
    public TrustDeviceDTO getTrustDeviceByAccount() {
        AccountDTO accountDTO = accountService.getAccountAuth();



        return null;
    }

    /**
     * @param filter
     * @param page
     * @param size
     * @return
     */
    @Override
    public PageDTO<TrustDeviceDTO> filter(TrustDeviceFilter filter, Integer page, Integer size) {
        log.info("{} Filtering trust devices with filter: {}, page: {}, size: {}", LOG_PREFIX, filter, page, size);

        AccountDTO accountDTO = accountService.getAccountAuth();
        filter.setAccountId(accountDTO.getAccountId());

        Specification<TrustDevice> specification = TrustDeviceSpecification.filter(filter);

        Pageable pageable = PageRequest.of(page, size);

        Page<TrustDevice> trustDevicePage = trustDeviceRepository.findAll(specification, pageable);

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

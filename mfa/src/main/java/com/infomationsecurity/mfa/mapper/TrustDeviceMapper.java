package com.infomationsecurity.mfa.mapper;

import com.infomationsecurity.mfa.dto.response.deviceDTO.TrustDeviceDTO;
import com.infomationsecurity.mfa.entity.TrustDevice;
import org.springframework.stereotype.Component;

@Component
public class TrustDeviceMapper {
    public TrustDeviceDTO entityToDTO(TrustDevice trustDevice){
        return TrustDeviceDTO.builder()
                .trustDeviceId(trustDevice.getDeviceId())
                .accountId(trustDevice.getAccount().getAccountId())
                .trustDeviceName(trustDevice.getDeviceName())
                .deviceIpAddress(trustDevice.getDeviceIpAddress())
                .deviceLocation(trustDevice.getDeviceLocation())
                .deviceIsActive(trustDevice.getDeviceIsActive())
                .deviceCreatedAt(trustDevice.getDeviceCreatedAt())
                .deviceUpdatedAt(trustDevice.getDeviceUpdatedAt() != null ? trustDevice.getDeviceUpdatedAt() : trustDevice.getDeviceCreatedAt())
                .build();
    }

    public TrustDevice createTrustDevice(String ip, String deviceName, String location) {
        return TrustDevice.builder()
                .deviceName(deviceName)
                .deviceIpAddress(ip)
                .deviceLocation(location)
                .deviceIsActive(true)
                .deviceIsVerified(false)
                .build();
    }


}

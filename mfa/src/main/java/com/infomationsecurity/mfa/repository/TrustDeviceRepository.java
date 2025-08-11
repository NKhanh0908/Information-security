package com.infomationsecurity.mfa.repository;

import com.infomationsecurity.mfa.entity.TrustDevice;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrustDeviceRepository extends JpaRepository<TrustDevice, Integer> {
    // Define custom query methods if needed
    // For example, to find trust devices by account ID or other criteria
    // Optional<TrustDevice> findByAccountId(Integer accountId);
    // Optional<TrustDevice> findByDeviceName(String deviceName);
}

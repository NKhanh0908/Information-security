package com.infomationsecurity.mfa.repository;

import com.infomationsecurity.mfa.entity.TrustDevice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface TrustDeviceRepository extends JpaRepository<TrustDevice, Integer> {
    // Define custom query methods if needed
    // For example, to find trust devices by account ID or other criteria
    // Optional<TrustDevice> findByAccountId(Integer accountId);
    // Optional<TrustDevice> findByDeviceName(String deviceName);

    @Query(value = """
    SELECT td.*
        FROM trust_device td
        WHERE td.device_ip_address = :device_ip_address
          AND td.device_name = :device_name
          AND td.device_location = :device_location;
    """,
    nativeQuery = true)
    Optional<TrustDevice> findByDeviceIpAddressAndDeviceNameAndDeviceLocation(@Param("device_ip_address") String deviceIpAddress,
                                                                           @Param("device_name") String deviceName,
                                                                           @Param("device_location") String deviceLocation);
}

package com.infomationsecurity.mfa.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trust_device")
public class TrustDevice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "device_id")
    private Integer deviceId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "device_name", nullable = false, length = 100)
    private String deviceName;

    @Column(name = "device_ip_address", nullable = false, length = 45)
    private String deviceIpAddress;

    @Column(name = "device_location", nullable = false)
    private String deviceLocation;

    @Column(name = "device_user_agent", nullable = false)
    private String deviceUserAgent;

    @Column(name = "device_is_active")
    private Boolean deviceIsActive = true;

    @Column(name = "device_created_at")
    private LocalDateTime deviceCreatedAt;

    @Column(name = "device_updated_at")
    private LocalDateTime deviceUpdatedAt;
}

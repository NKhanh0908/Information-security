package com.infomationsecurity.mfa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import io.swagger.v3.oas.models.security.SecurityScheme;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "activity_log")
public class ActivityLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "log_id")
    private Integer activityLogId;

    @Column(name = "account_id", nullable = false)
    private Integer accountId;

    @ManyToOne
    @JoinColumn(name = "device_id", nullable = false)
    private TrustDevice device;

    @Column(name = "log_action")
    private String logAction;

    @Column(name = "log_timestamp")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime logTimestamp;
}

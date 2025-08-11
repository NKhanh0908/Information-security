package com.infomationsecurity.mfa.entity;

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
@Table(name = "login_attempt")
public class LoginAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attempt_id")
    private Integer attemptId;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "attempt_ip_address", nullable = false, length = 45)
    private String attemptIpAddress;

    @Column(name = "attempt_success", nullable = false)
    private Boolean attemptSuccess;

    @Column(name = "attempt_user_agent", nullable = false)
    private String attemptUserAgent;

    @Column(name = "attempt_failure_reason")
    private String attemptFailureReason;

    @Column(name = "attempt_created_at")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime attemptCreatedAt;
}

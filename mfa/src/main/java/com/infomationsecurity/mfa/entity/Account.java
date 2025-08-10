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
@Table(name = "account")
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "account_id")
    private Integer accountId;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "account_username", nullable = false, length = 100)
    private String accountUsername;

    @Column(name = "account_password", nullable = false, length = 100)
    private String accountPassword;

    @Column(name = "account_email", nullable = false)
    private String accountEmail;

    @Column(name = "account_is_locked")
    private Boolean accountIsLocked = false;

    @Column(name = "account_locked_time")
    private LocalDateTime accountLockedTime;

    @Column(name = "account_last_login")
    private LocalDateTime accountLastLogin;

    @Column(name = "account_created_at")
    private LocalDateTime accountCreatedAt;

    @Column(name = "account_updated_at")
    private LocalDateTime accountUpdatedAt;
}

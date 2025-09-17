package com.infomationsecurity.mfa.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.infomationsecurity.mfa.config.BackupCodesConverter;
import com.infomationsecurity.mfa.enums.MfaMethod;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "mfa_settings")
public class MfaSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "mfa_id")
    private Integer mfaId;

    @OneToOne
    @JoinColumn(name = "account_id", nullable = false)
    @JsonBackReference
    private Account account;

    @Column(name = "mfa_enabled")
    private Boolean mfaEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_primary_method", nullable = false)
    private MfaMethod mfaPrimaryMethod = MfaMethod.EMAIL;

    @Enumerated(EnumType.STRING)
    @Column(name = "mfa_backup_method")
    private MfaMethod mfaBackupMethod;

    @Column(name = "mfa_totp_secret_key")
    private String mfaTotpSecretKey;

    @Column(name = "mfa_totp_enable")
    private Boolean mfaTotpEnable = false;

    @Column(name = "mfa_backup_codes")
    @Convert(converter = BackupCodesConverter.class)
    private List<String> backupCodes;

    @Column(name = "mfa_email_enabled")
    private Boolean mfaEmailEnabled = false;

    @Column(name = "mfa_webauthn_enabled")
    private Boolean mfaWebauthnEnabled = false;

    @Column(name = "mfa_authenticator_app_enabled")
    private Boolean mfaAuthenticatorAppEnabled = false;

    @Column(name = "mfa_required_mfa_for_sensitive_actions")
    private Boolean mfaRequiredMfaForSensitiveActions = false;

    @Column(name = "mfa_created_at")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime mfaCreatedAt;

    @Column(name = "mfa_updated_at")
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime mfaUpdatedAt;
}

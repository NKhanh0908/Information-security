package com.infomationsecurity.mfa.entity;

import com.infomationsecurity.mfa.enums.Gender;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "user_name", nullable = false, length = 100)
    private String userName;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_gender")
    private Gender userGender;

    @Column(name = "user_date_of_birth")
    private LocalDate userDateOfBirth;

    @Column(name = "user_address")
    private String userAddress;

    @Column(name = "user_phone", length = 20)
    private String userPhone;

    @Column(name = "user_created_at")
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime userCreatedAt;

    @Column(name = "user_updated_at")
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    private LocalDateTime userUpdatedAt;
}
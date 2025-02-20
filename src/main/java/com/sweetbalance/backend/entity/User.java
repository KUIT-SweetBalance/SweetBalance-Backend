package com.sweetbalance.backend.entity;

import com.sweetbalance.backend.enums.common.Status;
import com.sweetbalance.backend.enums.user.Gender;
import com.sweetbalance.backend.enums.user.LoginType;
import com.sweetbalance.backend.enums.user.Role;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "login_type", "deleted_at"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder @Getter @Setter
@ToString
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Column(length = 255)
    private String password;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;
}
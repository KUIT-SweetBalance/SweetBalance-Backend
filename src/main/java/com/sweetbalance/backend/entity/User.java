package com.sweetbalance.backend.entity;

import com.sweetbalance.backend.enums.common.Status;
import com.sweetbalance.backend.enums.user.Gender;
import com.sweetbalance.backend.enums.user.LoginType;
import com.sweetbalance.backend.enums.user.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"email", "login_type"})
})
@NoArgsConstructor
@AllArgsConstructor
@Builder @Getter @Setter
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // 기존 username을 email에 저장하게 해야함, 기존로직의 모든 username이 email을 대체하는 느낌
    @Column(nullable = false, length = 255)
    private String email;

    @Column(length = 50)
    private String nickname;

    @Column(length = 255)
    private String password;

    // 소셜 로그인 사용자의 서드파티이름_providerID 저장, 중복검사 시 ID가 아닌 providerId 기준 존재 여부 검사
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
}
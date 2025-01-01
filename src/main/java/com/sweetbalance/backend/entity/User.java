package com.sweetbalance.backend.entity;

import com.sweetbalance.backend.enums.common.Status;
import com.sweetbalance.backend.enums.user.Gender;
import com.sweetbalance.backend.enums.user.LoginType;
import com.sweetbalance.backend.enums.user.Role;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder @Getter @Setter
public class User extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long userId;

    // String 타입으로 바꿀지 고민
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    // BASIC USER 라면 로그인 ID, OAuth2 USER 라면 Provider ID 에 대응되는 값
    @Column(unique = true, nullable = false, length = 255)
    private String username;

    @Column(length = 50)
    private String nickname;

    @Column(length = 255)
    private String password;

    @Column(nullable = false, length = 255)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LoginType loginType;

    @Enumerated(EnumType.STRING)
    @Column
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column
    private Status status;
}

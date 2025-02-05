package com.sweetbalance.backend.dto.request;

import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.Gender;
import lombok.Getter;

import static com.sweetbalance.backend.enums.common.Status.ACTIVE;
import static com.sweetbalance.backend.enums.user.LoginType.BASIC;
import static com.sweetbalance.backend.enums.user.Role.USER;

@Getter
public class SignUpRequestDTO {
    private String email;
    private String password;
    private String nickname;
    private Gender gender;

    public User toActiveUser(){
        return User.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .gender(gender)
                .role(USER)
                .loginType(BASIC)
                .status(ACTIVE)
                .build();
    }
}

package com.sweetbalance.backend.dto.request;

import com.sweetbalance.backend.entity.User;
import lombok.Getter;

import static com.sweetbalance.backend.enums.common.Status.ACTIVE;
import static com.sweetbalance.backend.enums.user.LoginType.BASIC;
import static com.sweetbalance.backend.enums.user.Role.USER;

@Getter
public class SignUpRequestDTO {
    private String username;

    private String password;

    private String email;

    public User toActiveUser(){
        return User.builder()
                .username(username).password(password).email(email)
                .role(USER).loginType(BASIC).status(ACTIVE).build();
    }
}

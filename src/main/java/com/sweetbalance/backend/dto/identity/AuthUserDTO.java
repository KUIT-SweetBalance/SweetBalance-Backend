package com.sweetbalance.backend.dto.identity;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AuthUserDTO {

    private Long userId;

    private String role;

    private String username;
}
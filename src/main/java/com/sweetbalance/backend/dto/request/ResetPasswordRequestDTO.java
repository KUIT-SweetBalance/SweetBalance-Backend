package com.sweetbalance.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class ResetPasswordRequestDTO {
    private String email;
    private String newPassword;
}

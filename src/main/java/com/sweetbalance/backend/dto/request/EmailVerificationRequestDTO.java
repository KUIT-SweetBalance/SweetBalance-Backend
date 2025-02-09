package com.sweetbalance.backend.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter

public class EmailVerificationRequestDTO {
    private String email;
    private String code;
}

package com.sweetbalance.backend.dto.response.token;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class TokenPairDTO {

    private final String access;

    private final String refresh;
}

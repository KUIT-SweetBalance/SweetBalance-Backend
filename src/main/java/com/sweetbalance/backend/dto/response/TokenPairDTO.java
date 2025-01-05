package com.sweetbalance.backend.dto.response;

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

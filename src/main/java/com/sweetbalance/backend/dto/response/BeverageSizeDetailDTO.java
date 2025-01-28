package com.sweetbalance.backend.dto.response;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class BeverageSizeDetailDTO {
    private Long id;
    private String sizeType;
    private int volume;

    private int sugar;
    private int calories;
    private int caffeine;
}
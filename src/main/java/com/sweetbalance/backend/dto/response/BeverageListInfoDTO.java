package com.sweetbalance.backend.dto.response;

import com.sweetbalance.backend.enums.beverage.BeverageCategory;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class BeverageListInfoDTO {
    private Long beverageId;

    private String name;
    private String brand;
    private String imgUrl;
    private BeverageCategory category;

    private double sugar;
    private double calories;
    private double caffeine;
    private Integer consumeCount;

    private List<BeverageSizeDetailDTO> sizes;
}

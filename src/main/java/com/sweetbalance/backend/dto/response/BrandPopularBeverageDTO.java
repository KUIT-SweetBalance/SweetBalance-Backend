package com.sweetbalance.backend.dto.response;

import com.sweetbalance.backend.enums.beverage.BeverageCategory;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class BrandPopularBeverageDTO {
    private Long beverageId;

    private String name;
    private String brand;
    private String imgUrl;
    private Integer consumeCount;
}

package com.sweetbalance.backend.dto.response;

import com.sweetbalance.backend.enums.beverage.BeverageCategory;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class ListBeverageDTO {
    private Long favoriteId;
    private Long beverageId;

    private String name;
    private String brand;
    private String imgUrl;
    private BeverageCategory category;

    private double sugar;
    private double calories;
    private double caffeine;

    private String timeString;
}

package com.sweetbalance.backend.dto.response;

import com.sweetbalance.backend.enums.beverage.BeverageCategory;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class FavoriteBeverageDTO {
    private Long favoriteId;
    private Long beverageId;

    private String name;
    private String brand;
    private String imgUrl;

    private int sugarPer100ml;
    private String timeString;
}

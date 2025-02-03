package com.sweetbalance.backend.dto.response;

import com.sweetbalance.backend.enums.beverage.BeverageCategory;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class RecommendedBeverageDTO {
    private Long beverageId;

    private String name;
    private String brand;
    private String imgUrl;

    private String sizeType;
    private String sizeTypeDetail;
    private int volume;
    private double sugarGap;
}

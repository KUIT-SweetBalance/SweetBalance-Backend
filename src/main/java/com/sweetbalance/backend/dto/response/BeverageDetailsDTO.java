package com.sweetbalance.backend.dto.response;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class BeverageDetailsDTO {
    private Long beverageId;

    private String name;
    private String brand;
    private String imgUrl;
    private boolean favorite;

    private List<String> syrups;
    private List<BeverageSizeDetailsWithRecommendDTO> sizeDetails;
}

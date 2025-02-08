package com.sweetbalance.backend.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class InnerListBeverageDTO {
    private Long beverageId;

    private String name;
    private String brand;
    private String imgUrl;
    private int sugarPer100ml;
    private boolean favorite;
}

package com.sweetbalance.backend.dto.response;

import com.sweetbalance.backend.entity.Beverage;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class BeverageSizeDetailsWithRecommendDTO {
    private Long id;

    private String sizeType;
    private String sizeTypeDetail;
    private int volume;
    private int sugar;
    private int calories;
    private int caffeine;

    private List<RecommendedBeverageDTO> recommends;
}

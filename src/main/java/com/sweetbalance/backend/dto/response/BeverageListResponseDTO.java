package com.sweetbalance.backend.dto.response;

import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class BeverageListResponseDTO {
    private int totalBeverageNum;
    private List<InnerListBeverageDTO> beverages;
}

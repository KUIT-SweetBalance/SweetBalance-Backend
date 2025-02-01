package com.sweetbalance.backend.dto.response;

import java.time.LocalDate;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class DailySugarDTO {
    private LocalDate date;
    private double sugar;
}
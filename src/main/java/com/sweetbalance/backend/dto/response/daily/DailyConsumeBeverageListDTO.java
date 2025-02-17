package com.sweetbalance.backend.dto.response.daily;

import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.entity.BeverageSize;
import lombok.*;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@NoArgsConstructor
@AllArgsConstructor
@Getter @Setter @Builder
public class DailyConsumeBeverageListDTO {

    private Long beverageLogId;
    private Long beverageId;
    private Long beverageSizeId;
    private String createdAt;
    private String brand;
    private String beverageName;
    private String imgUrl;
    private int sugar;
    private String syrupName;
    private int syrupCount;
    private String sizeType;

    public static DailyConsumeBeverageListDTO fromEntity(BeverageLog log) {
        BeverageSize beverageSize = log.getBeverageSize();
        Beverage beverage = beverageSize.getBeverage();

        DateTimeFormatter dateTimeFormat = DateTimeFormatter
                .ofPattern("yyyy.MM.dd (E) HH:mm")
                .withLocale(Locale.KOREAN);

        String formattedDateTime = log.getCreatedAt().format(dateTimeFormat);

        double baseSugar = beverageSize.getSugar();
        double additionalSugar = log.getAdditionalSugar();
        double totalSugar = baseSugar + additionalSugar;
        int roundedSugar = (int) Math.round(totalSugar);

        return DailyConsumeBeverageListDTO.builder()
                .beverageLogId(log.getLogId())
                .beverageId(beverage.getBeverageId())
                .beverageSizeId(beverageSize.getId())
                .createdAt(formattedDateTime)
                .brand(beverage.getBrand())
                .beverageName(beverage.getName())
                .imgUrl(beverage.getImgUrl())
                .sugar(roundedSugar)
                .syrupName(log.getSyrupName())
                .syrupCount(log.getSyrupCount())
                .sizeType(beverageSize.getSizeType())
                .build();
    }
}


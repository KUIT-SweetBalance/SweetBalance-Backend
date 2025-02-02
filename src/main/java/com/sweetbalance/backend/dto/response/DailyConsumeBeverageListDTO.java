package com.sweetbalance.backend.dto.response;

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

    private String createdAt;
    private String beverageName;
    private String imgUrl;
    private int sugar;
    private String syrupName;
    private int syrupCount;
    private String sizeType;

    public static DailyConsumeBeverageListDTO fromEntity(BeverageLog log) {
        BeverageSize beverageSize = log.getBeverageSize();
        Beverage beverage = beverageSize.getBeverage();

        DateTimeFormatter dtf = DateTimeFormatter
                .ofPattern("yyyy.MM.dd (E) HH:mm")
                .withLocale(Locale.KOREAN);

        String formattedDateTime = log.getCreatedAt().format(dtf);

        double rawSugar = beverageSize.getSugar();
        int roundedSugar = (int) Math.round(rawSugar);

        return DailyConsumeBeverageListDTO.builder()
                .createdAt(formattedDateTime)
                .beverageName(beverage.getName())
                .imgUrl(beverage.getImgUrl())
                .sugar(roundedSugar)
                .syrupName(log.getSyrupName())
                .syrupCount(log.getSyrupCount())
                .sizeType(beverageSize.getSizeType())
                .build();
    }
}


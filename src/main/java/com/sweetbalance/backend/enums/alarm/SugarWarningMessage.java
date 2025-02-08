package com.sweetbalance.backend.enums.alarm;

import lombok.Getter;

@Getter
public enum SugarWarningMessage {
    CAUTION("당 20g 기록, 주의 필요!"), EXCEED("일일 당 권장량(25g) 초과 섭취!");

    final String message;

    private SugarWarningMessage(String message){
        this.message = message;
    }

}

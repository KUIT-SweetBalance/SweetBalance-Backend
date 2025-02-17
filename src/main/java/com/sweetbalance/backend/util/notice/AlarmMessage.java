package com.sweetbalance.backend.util.notice;

import com.sweetbalance.backend.enums.alarm.SugarWarning;
import com.sweetbalance.backend.enums.user.Gender;

public class AlarmMessage {

    public static String of(SugarWarning sw, Gender gender) {
        return switch (sw) {
            case CAUTION -> switch (gender) {
                case MALE -> "당 33g 기록, 주의 필요!";
                case FEMALE -> "당 20g 기록, 주의 필요!";
            };
            case EXCEED -> switch (gender) {
                case MALE -> "일일 당 권장량(38g) 초과 섭취!";
                case FEMALE -> "일일 당 권장량(25g) 초과 섭취!";
            };
            default -> throw new IllegalArgumentException("AlarmMessage.of의 매개변수가 잘못되었습니다: " + sw + ", " + gender);
        };
    }

}

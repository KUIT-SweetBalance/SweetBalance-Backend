package com.sweetbalance.backend.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class TimeStringConverter {

    /*
     * LocalDateTime 객체를 인자로 받아 문자열 반환
     * EX) 2024.11.26(화) 13:23
     */
    public static String convertLocalDateTimeToKoreanTimeString(LocalDateTime localDateTime){
        String[] days = {"월", "화", "수", "목", "금", "토", "일"};
        DayOfWeek dayOfWeek = localDateTime.getDayOfWeek();
        String koreanDay = days[dayOfWeek.getValue() % 7]; // Java의 DayOfWeek는 월요일=1, 일요일=7

        // 원하는 포맷 적용
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd", Locale.KOREAN);
        String formattedDate = localDateTime.format(formatter);

        // 최종 결과 조합
        return formattedDate + "(" + koreanDay + ") " + localDateTime.format(DateTimeFormatter.ofPattern("HH:mm"));
    }
}

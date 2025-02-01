package com.sweetbalance.backend.util;

import java.util.HashMap;
import java.util.Map;

public class SyrupToSugarMapper {
    private static final Map<String, Double> sugarInSyrupInfoMap = new HashMap<>();

    static {
        // 커피
        sugarInSyrupInfoMap.put("바닐라", 5.0);
        sugarInSyrupInfoMap.put("카라멜", 5.5);
        sugarInSyrupInfoMap.put("헤이즐넛", 5.0);
        sugarInSyrupInfoMap.put("시나몬", 5.8);

        // 라떼
        sugarInSyrupInfoMap.put("초콜릿", 6.0);

        // 티
        sugarInSyrupInfoMap.put("꿀 시럽", 6.0);
        sugarInSyrupInfoMap.put("자몽 시럽", 5.5);
        sugarInSyrupInfoMap.put("레몬 시럽", 5.5);
        sugarInSyrupInfoMap.put("복숭아 시럽", 5.0);
    }

    public static double getAmountOfSugar(String syrupName) {
        if (!sugarInSyrupInfoMap.containsKey(syrupName)) {
            throw new IllegalArgumentException("해당 시럽에 대한 설탕 함량 정보가 없습니다: " + syrupName);
        }
        return sugarInSyrupInfoMap.get(syrupName);
    }
}

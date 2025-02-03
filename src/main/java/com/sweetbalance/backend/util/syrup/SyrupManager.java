package com.sweetbalance.backend.util.syrup;

import java.util.*;

public final class SyrupManager {

    // 브랜드명, 각 브랜드의 시럽 리스트
    private static final Map<String, List<Syrup>> syrupRepository;

    static {
        Map<String, List<Syrup>> repo = new HashMap<>();

        // 스타벅스 시럽 정보
        repo.put("스타벅스", Collections.unmodifiableList(Arrays.asList(
                Syrup.of("헤이즐넛", 11.0),
                Syrup.of("바닐라", 7.0),
                Syrup.of("슈가프리 바닐라", 0.0),
                Syrup.of("캐러맬", 5.0),
                Syrup.of("시나몬 돌체", 5.0),
                Syrup.of("토피넛", 5.0),
                Syrup.of("클래식", 3.5),
                Syrup.of("자몽", 8.0)
        )));

        // 투썸플레이스 시럽 정보
        repo.put("투썸플레이스", Collections.unmodifiableList(Arrays.asList(
                Syrup.of("바닐라", 11.0),
                Syrup.of("카라멜", 6.0),
                Syrup.of("헤이즐넛", 6.0)
        )));

        // 메가커피 시럽 정보
        repo.put("메가커피", Collections.unmodifiableList(Arrays.asList(
                Syrup.of("바닐라", 7.0),
                Syrup.of("헤이즐넛", 7.0),
                Syrup.of("카라멜", 8.0),
                Syrup.of("초코시럽", 8.0),
                Syrup.of("자몽", 6.0),
                Syrup.of("꿀", 7.0),
                Syrup.of("레몬", 6.0),
                Syrup.of("흑당시럽", 8.0)
        )));

        // 빽다방 시럽 정보
        repo.put("빽다방", Collections.unmodifiableList(Arrays.asList(
                Syrup.of("바닐라", 7.0),
                Syrup.of("헤이즐넛", 7.0),
                Syrup.of("카라멜", 8.0),
                Syrup.of("꿀", 7.0),
                Syrup.of("자몽", 6.0),
                Syrup.of("흑당", 8.0)
        )));

        // 컴포즈커피 시럽 정보
        repo.put("컴포즈커피", Collections.unmodifiableList(Arrays.asList(
                Syrup.of("바닐라", 7.0),
                Syrup.of("헤이즐넛", 7.0),
                Syrup.of("메이플", 7.0),
                Syrup.of("카라멜", 8.0),
                Syrup.of("꿀", 7.0),
                Syrup.of("흑당", 8.0),
                Syrup.of("자몽", 6.0)
        )));

        syrupRepository = Collections.unmodifiableMap(repo);
    }

    private SyrupManager() {
        throw new UnsupportedOperationException("SyrupManager는 유틸리티 클래스로, 인스턴스화할 수 없습니다.");
    }

    public static double getAmountOfSugar(String brandName, String syrupName) {

        List<Syrup> syrups  = syrupRepository.get(brandName);
        if(syrups == null){
            throw new IllegalArgumentException("해당 브랜드에 대한 시럽 함량 정보가 없습니다: " + brandName);
        }

        return syrups.stream()
                .filter(syrup -> syrup.getSyrupName().equals(syrupName))
                .findFirst()
                .map(Syrup::getSugarPerPump)
                .orElseThrow(() -> new IllegalArgumentException("해당 브랜드의 시럽 목록에 일치하는 시럽 정보가 없습니다: " + syrupName));
    }

    public static List<Syrup> getSyrupListOfBrand(String brandName){
        return syrupRepository.get(brandName);
    }
}

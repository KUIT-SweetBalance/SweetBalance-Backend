package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.identity.UserIdHolder;
import com.sweetbalance.backend.dto.response.daily.DailyConsumeBeverageListDTO;
import com.sweetbalance.backend.dto.response.daily.DailyConsumeInfoDTO;
import com.sweetbalance.backend.dto.response.weekly.WeeklyConsumeInfoDTO;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.Gender;
import com.sweetbalance.backend.service.BeverageLogDataService;
import com.sweetbalance.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class BeverageLogDataController {

    private final UserService userService;
    private final BeverageLogDataService beverageLogDataService;

    @GetMapping("/daily-brand-list")
    public ResponseEntity<?> getDailyConsumeBrandListOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

        List<BeverageLog> dailyBrandLogs = beverageLogDataService.findTodayBeverageLogsByUserId(userId);

        List<String> brandList = dailyBrandLogs.stream()
                .map(log -> log.getBeverageSize().getBeverage().getBrand())
                .distinct()
                .toList();

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("오늘 섭취한 브랜드 리스트 조회 성공", brandList)
        );
    }

    @GetMapping("/daily-beverage-list")
    public ResponseEntity<?> getDailyConsumeBeverageListOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

        List<BeverageLog> dailyBeverageLogs = beverageLogDataService.findTodayBeverageLogsByUserId(userId);

        List<DailyConsumeBeverageListDTO> todayConsumeBeverageList = dailyBeverageLogs.stream()
                .map(DailyConsumeBeverageListDTO::fromEntity)
                .toList();

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("오늘 섭취한 음료 리스트 조회 성공", todayConsumeBeverageList)
        );
    }

    @GetMapping("/daily-consume-info")
    public ResponseEntity<?> getDailyConsumeInfoOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

        User user = userOptional.get();

        List<BeverageLog> dailyBeverageLogs = beverageLogDataService.findTodayBeverageLogsByUserId(userId);

        double initSugarSum = 0.0;
        for (BeverageLog log : dailyBeverageLogs) {
            initSugarSum += log.getBeverageSize().getSugar();
        }

        int totalSugar = (int) Math.round(initSugarSum);

        int beverageCount = dailyBeverageLogs.size();

        int unreadAlarmCount = beverageLogDataService.getNumberOfUnreadLogWithinAWeek();

        int additionalSugar = 0;
        if (user.getGender() == Gender.MALE) {
            additionalSugar = 38 - totalSugar;
        } else if (user.getGender() == Gender.FEMALE) {
            additionalSugar = 25 - totalSugar;
        }

        DailyConsumeInfoDTO dailyConsumeInfo = DailyConsumeInfoDTO.builder()
                .totalSugar(totalSugar)
                .additionalSugar(additionalSugar)
                .beverageCount(beverageCount)
                .unreadAlarmCount(unreadAlarmCount)
                .build();

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("오늘 영양섭취 정보 조회 성공", dailyConsumeInfo)
        );
    }

    @GetMapping("/weekly-consume-info")
    public ResponseEntity<?> getWeeklyConsumeInfo(
            @AuthenticationPrincipal UserIdHolder userIdHolder,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

        try{

            Long userId = userIdHolder.getUserId();
            LocalDate endDate = (startDate != null) ? startDate.plusDays(6) : LocalDate.now();
            startDate = (startDate != null) ? startDate : endDate.minusDays(6);

            WeeklyConsumeInfoDTO weeklyConsumeInfoDTO = beverageLogDataService.getWeeklyConsumeInfo(userId, startDate, endDate);

            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.success("주간 영양정보 반환 성공", weeklyConsumeInfoDTO)
            );
        } catch (Exception e){

            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "주간 영양정보 반환 실패")
            );
        }
    }

    // 상단 하단 메소드들 클래스 분리 고려

    @GetMapping("/beverage-record")
    public ResponseEntity<?> getTotalBeverageListOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder,
                                                          @RequestParam("page") int page,
                                                          @RequestParam("size") int size) {
        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

        Pageable pageable = PageRequest.of(page, size);

        List<BeverageLog> beverageLogs = beverageLogDataService.findTotalBeverageLogsByUserId(userId, pageable);

        List<DailyConsumeBeverageListDTO> dailyConsumeBeverageList = new ArrayList<>();
        for (BeverageLog log : beverageLogs) {
            dailyConsumeBeverageList.add(DailyConsumeBeverageListDTO.fromEntity(log));
        }

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("전체 섭취 음료 리스트 조회 성공", dailyConsumeBeverageList)
        );
    }
}

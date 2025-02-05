package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.identity.UserIdHolder;
import com.sweetbalance.backend.dto.response.*;
import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.entity.BeverageSize;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.service.BeverageService;
import com.sweetbalance.backend.service.BeverageSizeService;
import com.sweetbalance.backend.service.UserService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.*;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final BeverageService beverageService;
    private final BeverageSizeService beverageSizeService;


    @GetMapping("/my-info")
    public ResponseEntity<?> findClientInfo(@AuthenticationPrincipal UserIdHolder userIdHolder) {

        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);

        if (userOptional.isPresent()) {

            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.success("본인 정보 반환 성공", userOptional.get())
            );
        } else {

            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }
    }

    @GetMapping("/favorite")
    public ResponseEntity<?> getFavoriteList(@AuthenticationPrincipal UserIdHolder userIdHolder,
                                             @RequestParam("page") int page,
                                             @RequestParam("size") int size) {
        Long userId = userIdHolder.getUserId();
        Pageable pageable = PageRequest.of(page, size);

        List<FavoriteBeverageDTO> listBeverages = userService.getFavoriteListByUserId(userId, pageable);

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("즐겨찾기 음료 리스트 반환 성공", listBeverages)
        );
    }

    @GetMapping("/weekly-consume-info")
    public ResponseEntity<?> getWeeklyConsumeInfo(
            @AuthenticationPrincipal UserIdHolder userIdHolder,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate) {

        Long userId = userIdHolder.getUserId();
        LocalDate endDate = (startDate != null) ? startDate.plusDays(6) : LocalDate.now();
        startDate = (startDate != null) ? startDate : endDate.minusDays(6);

        WeeklyInfoDTO weeklyInfoDTO = userService.getWeeklyConsumeInfo(userId, startDate, endDate);

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("주간 영양정보 반환 성공", weeklyInfoDTO)
        );
    }

    @PostMapping("/meta-data")
    public ResponseEntity<?> setMetaDataOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder, @RequestBody MetadataRequestDTO metaDataRequestDTO){

        Long userId = userIdHolder.getUserId();
        Optional<User> userOptional = userService.findUserByUserId(userId);

        if (userOptional.isPresent()) {
            userService.updateMetaData(userOptional.get(), metaDataRequestDTO);
            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.success("메타데이터 업데이트 성공", null)
            );
        } else {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

    }

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

        List<BeverageLog> beverageLogs = userService.findTotalBeverageLogsByUserId(userId, pageable);

        List<DailyConsumeBeverageListDTO> dailyConsumeBeverageList = new ArrayList<>();
        for (BeverageLog log : beverageLogs) {
            dailyConsumeBeverageList.add(DailyConsumeBeverageListDTO.fromEntity(log));
        }

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("전체 섭취 음료 리스트 조회 성공", dailyConsumeBeverageList)
        );
    }


    @GetMapping("/daily-brand-list")
    public ResponseEntity<?> getDailyConsumeBrandListOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

        List<BeverageLog> dailyBrandLogs = userService.findTodayBeverageLogsByUserId(userId);

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

        List<BeverageLog> dailyBeverageLogs = userService.findTodayBeverageLogsByUserId(userId);

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

        List<BeverageLog> dailyBeverageLogs = userService.findTodayBeverageLogsByUserId(userId);

        double initSugarSum = 0.0;
        for (BeverageLog log : dailyBeverageLogs) {
            initSugarSum += log.getBeverageSize().getSugar();
        }

        int totalSugar = (int) Math.round(initSugarSum);

        int beverageCount = dailyBeverageLogs.size();

        DailyConsumeInfoDTO dailyConsumeInfo = DailyConsumeInfoDTO.builder()
                .totalSugar(totalSugar)
                .beverageCount(beverageCount)
                .build();

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("오늘 영양섭취 정보 조회 성공", dailyConsumeInfo)
        );
    }

//    @GetMapping("/api/user/notice-list")
//    public ResponseEntity<?> getNoticeListOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
//
//    }

    @PostMapping("/beverage-record")
    public ResponseEntity<?> addBeverageRecord(@AuthenticationPrincipal UserIdHolder userIdHolder,
                                               @RequestBody AddBeverageRecordRequestDTO dto) {
        try {
            User user = getUser(userIdHolder.getUserId());
            BeverageSize beverageSize = getBeverageSize(dto.getBeverageSizeId());

            userService.addBeverageRecord(user, beverageSize, dto);
            return ResponseEntity.ok(DefaultResponseDTO.success("음료 섭취 기록 추가 성공", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(DefaultResponseDTO.error(404, 999, e.getMessage()));
        }
    }

    @PostMapping("/beverage-record/{beverageLogId}")
    public ResponseEntity<?> editBeverageRecord(@AuthenticationPrincipal UserIdHolder userIdHolder, @PathVariable("beverageLogId") Long beverageLogId, @RequestBody AddBeverageRecordRequestDTO dto){

        try {
            // 사용자 검증
            getUser(userIdHolder.getUserId());

            // 수정할 BeverageLog와 새 BeverageSize 조회
            BeverageLog beverageLog = getBeverageLog(beverageLogId);
            BeverageSize beverageSize = getBeverageSize(dto.getBeverageSizeId());

            userService.editBeverageRecord(beverageLogId, beverageSize, dto);
            return ResponseEntity.ok(DefaultResponseDTO.success("음료 섭취 기록 수정 성공", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(DefaultResponseDTO.error(404, 999, e.getMessage()));
        }
    }

    @DeleteMapping("/beverage-record/{beverageLogId}")
    public ResponseEntity<?> deleteBeverageRecord(@AuthenticationPrincipal UserIdHolder userIdHolder,
                                               @PathVariable("beverageLogId") Long beverageLogId){
        try {
            // 사용자 검증
            getUser(userIdHolder.getUserId());
            BeverageLog beverageLog = getBeverageLog(beverageLogId);
            userService.deleteBeverageRecord(beverageLog);
            return ResponseEntity.ok(DefaultResponseDTO.success("음료 섭취 기록 삭제 성공", null));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(DefaultResponseDTO.error(404, 999, e.getMessage()));
        }
    }

    @GetMapping("/notice-list")
    public ResponseEntity<?> getNoticeList(@AuthenticationPrincipal UserIdHolder userIdHolder,
                                           @RequestParam("page") int page,
                                           @RequestParam("size") int size) {
        Long userId = userIdHolder.getUserId();
        Pageable pageable = PageRequest.of(page, size);

        List<ListNoticeDTO> listBeverages = userService.getNoticeListByUserId(userId,pageable);

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("알림 리스트 반환 성공", listBeverages)
        );
    }

    @PostMapping("/favorite/{beverageId}")
    public ResponseEntity<?> addFavorite(@AuthenticationPrincipal UserIdHolder userIdHolder, @PathVariable("beverageId") Long beverageId){
        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

        Optional<Beverage> beverageOptional = beverageService.findBeverageByBeverageId(beverageId);
        if (beverageOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 음료 정보를 찾을 수 없습니다.")
            );
        }

        userService.addFavoriteRecord(userOptional.get(), beverageOptional.get());

        return ResponseEntity.ok(
                DefaultResponseDTO.success("즐겨찾기 추가 성공", null)
        );
    }

    @DeleteMapping("/favorite/{beverageId}")
    public ResponseEntity<?> deleteFavorite(@AuthenticationPrincipal UserIdHolder userIdHolder, @PathVariable("beverageId") Long beverageId){
        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

        Optional<Beverage> beverageOptional = beverageService.findBeverageByBeverageId(beverageId);
        if (beverageOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 음료 정보를 찾을 수 없습니다.")
            );
        }

        userService.deleteFavoriteRecord(userOptional.get(), beverageOptional.get());

        return ResponseEntity.ok(
                DefaultResponseDTO.success("즐겨찾기 삭제 성공", null)
        );
    }

    /**
     * 사용자 ID를 통해 User 객체를 조회
     * 없을 경우 IllegalArgumentException을 발생
     */
    private User getUser(Long userId) {
        return userService.findUserByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 User 정보를 찾을 수 없습니다."));
    }

    /**
     * BeverageSize ID를 통해 BeverageSize 객체를 조회
     * 없을 경우 IllegalArgumentException을 발생
     */
    private BeverageSize getBeverageSize(Long beverageSizeId) {
        return beverageSizeService.findBeverageSizeByBeverageSizeId(beverageSizeId)
                .orElseThrow(() -> new IllegalArgumentException("등록된 음료 사이즈 정보를 찾을 수 없습니다."));
    }

    /**
     * BeverageLog ID를 통해 BeverageLog 객체를 조회
     * 없을 경우 IllegalArgumentException을 발생
     */
    private BeverageLog getBeverageLog(Long beverageLogId) {
        return userService.findBeverageLogByBeverageLogId(beverageLogId)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 음료 기록을 찾을 수 없습니다."));
    }

}

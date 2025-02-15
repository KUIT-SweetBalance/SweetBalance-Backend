package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.identity.UserIdHolder;
import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.response.daily.DailyConsumeBeverageListDTO;
import com.sweetbalance.backend.dto.response.daily.DailyConsumeInfoDTO;
import com.sweetbalance.backend.dto.response.weekly.WeeklyConsumeInfoDTO;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.entity.BeverageSize;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.Gender;
import com.sweetbalance.backend.service.BeverageSizeService;
import com.sweetbalance.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class BeverageLogManageController {

    private final UserService userService;
    private final BeverageSizeService beverageSizeService;

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

package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.identity.UserIdHolder;
import com.sweetbalance.backend.dto.response.notice.ListNoticeDTO;
import com.sweetbalance.backend.service.NoticeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Notice", description = "알림 관련 API")
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @Operation(summary = "알림 리스트 조회")
    @GetMapping("/notice-list")
    public ResponseEntity<?> getNoticeList(@AuthenticationPrincipal UserIdHolder userIdHolder) {
        Long userId = userIdHolder.getUserId();

        List<ListNoticeDTO> listBeverages = noticeService.getNoticeListByUserId(userId);

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("알림 리스트 반환 성공", listBeverages)
        );
    }

    @Operation(summary = "유저가 특정 알림을 읽었다고 표시")
    @PostMapping("/notice/{beverageLogId}")
    public ResponseEntity<?> checkAlarmReaded(@AuthenticationPrincipal UserIdHolder userIdHolder, @PathVariable("beverageLogId") Long beverageLogId){

        noticeService.checkNoticeReaded(beverageLogId);
        return ResponseEntity.ok(DefaultResponseDTO.success("알람 읽음 기록 추가 성공", null));
    }
}

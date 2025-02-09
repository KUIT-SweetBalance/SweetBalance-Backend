package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.identity.UserIdHolder;
import com.sweetbalance.backend.dto.response.notice.ListNoticeDTO;
import com.sweetbalance.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class NoticeController {

    private final UserService userService;

    @GetMapping("/notice-list")
    public ResponseEntity<?> getNoticeList(@AuthenticationPrincipal UserIdHolder userIdHolder) {
        Long userId = userIdHolder.getUserId();

        List<ListNoticeDTO> listBeverages = userService.getNoticeListByUserId(userId);

        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("알림 리스트 반환 성공", listBeverages)
        );
    }

    @PostMapping("/notice/{beverageLogId}")
    public ResponseEntity<?> checkAlarmReaded(@AuthenticationPrincipal UserIdHolder userIdHolder, @PathVariable("beverageLogId") Long beverageLogId){

        userService.checkNoticeReaded(beverageLogId);
        return ResponseEntity.ok(DefaultResponseDTO.success("알람 읽음 기록 추가 성공", null));
    }
}

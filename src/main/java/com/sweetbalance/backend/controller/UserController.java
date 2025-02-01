package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.identity.UserIdHolder;
import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.entity.BeverageSize;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.service.BeverageService;
import com.sweetbalance.backend.service.BeverageSizeService;
import com.sweetbalance.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
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

    @GetMapping("/beverage-record")
    public ResponseEntity<?> getBeveragesOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder) {

        Long userId = userIdHolder.getUserId();

        List<Beverage> beverages = userService.findBeveragesByUserId(userId);
        
        return ResponseEntity.status(200).body(
                DefaultResponseDTO.success("음료 리스트 반환 성공", beverages)
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

//    @GetMapping("/api/user/favorite")
//    public ResponseEntity<?> getFavoriteOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
//
//    }
//
//    @GetMapping("/api/user/daily-brand-list")
//    public ResponseEntity<?> getDailyBrandListOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
//
//    }
//
//    @GetMapping("/api/user/daily-beverage-list")
//    public ResponseEntity<?> getDailyBeverageListOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
//
//    }
//
//    @GetMapping("/api/user/daily-consume-info")
//    public ResponseEntity<?> getDailyConsumeInfoOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
//
//    }
//
//    @GetMapping("/api/user/weekly-consume-info")
//    public ResponseEntity<?> getWeeklyConsumeInfoOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
//
//    }
//
//    @GetMapping("/api/user/notice-list")
//    public ResponseEntity<?> getNoticeListOfClient(@AuthenticationPrincipal UserIdHolder userIdHolder){
//
//    }
//
//
    @PostMapping("/beverage-record")
    public ResponseEntity<?> addBeverageRecord(@AuthenticationPrincipal UserIdHolder userIdHolder,
                                               @RequestBody AddBeverageRecordRequestDTO dto){

        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

        Optional<BeverageSize> beverageSizeOptional = beverageSizeService.findBeverageSizeByBeverageSizeId(dto.getBeverageSizeId());
        if (beverageSizeOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 음료 사이즈 정보를 찾을 수 없습니다.")
            );
        }

        try {
            userService.addBeverageRecord(userOptional.get(), beverageSizeOptional.get(), dto);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "일치하는 시럽 정보를 찾을 수 없습니다.")
            );
        }

        return ResponseEntity.ok(
                DefaultResponseDTO.success("음료 섭취 기록 추가 성공", null)
        );
    }

    @PostMapping("/beverage-record/{beverageLogId}")
    public ResponseEntity<?> editBeverageRecord(@AuthenticationPrincipal UserIdHolder userIdHolder, @PathVariable("beverageLogId") Long beverageLogId, @RequestBody AddBeverageRecordRequestDTO dto){

        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

        Optional<BeverageLog> beverageLogOptional = userService.findBeverageLogByBeverageLogId(beverageLogId);
        if(beverageLogOptional.isEmpty()){
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "일치하는 음료 기록을 찾을 수 없습니다.")
            );
        }

        Optional<BeverageSize> beverageSizeOptional = beverageSizeService.findBeverageSizeByBeverageSizeId(dto.getBeverageSizeId());
        if (beverageSizeOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 음료 사이즈 정보를 찾을 수 없습니다.")
            );
        }

        try {
            userService.editBeverageRecord(beverageLogId, beverageSizeOptional.get(), dto);
        } catch (IllegalArgumentException e){
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "일치하는 시럽 정보를 찾을 수 없습니다.")
            );
        }

        return ResponseEntity.ok(
                DefaultResponseDTO.success("음료 섭취 기록 수정 성공", null)
        );
    }

    @DeleteMapping("/beverage-record/{beverageLogId}")
    public ResponseEntity<?> deleteBeverageRecord(@AuthenticationPrincipal UserIdHolder userIdHolder,
                                               @PathVariable("beverageLogId") Long beverageLogId){
        Long userId = userIdHolder.getUserId();

        Optional<User> userOptional = userService.findUserByUserId(userId);
        if (userOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 User 정보를 찾을 수 없습니다.")
            );
        }

        Optional<BeverageLog> logOptional = userService.findBeverageLogByBeverageLogId(beverageLogId);
        if (logOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "일치하는 음료 기록을 찾을 수 없습니다.")
            );
        }

        userService.deleteBeverageRecord(logOptional.get());
        return ResponseEntity.ok(
                DefaultResponseDTO.success("음료 섭취 기록 삭제 성공", null)
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

}

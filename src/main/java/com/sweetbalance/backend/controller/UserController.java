package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.identity.UserIdHolder;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @Autowired
    public UserController(UserService userService){
        this.userService = userService;
    }

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
//    @PostMapping("/api/user/beverage-record/{beverage-id}")
//    public ResponseEntity<?> addBeverageRecord(@AuthenticationPrincipal UserIdHolder userIdHolder, @PathVariable("beverage-id") String parameter){
//
//    }
//
//    @PostMapping("/api/user/favorite/{beverage-id}")
//    public ResponseEntity<?> addFavorite(@AuthenticationPrincipal UserIdHolder userIdHolder, @PathVariable("beverage-id") String parameter){
//
//    }
//
//    @DeleteMapping("/api/user/favorite/{beverage-id}")
//    public ResponseEntity<?> deleteFavorite(@AuthenticationPrincipal UserIdHolder userIdHolder, @PathVariable("beverage-id") String parameter){
//
//    }

}

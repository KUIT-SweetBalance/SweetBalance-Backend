package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.identity.UserIdHolder;
import com.sweetbalance.backend.dto.response.FavoriteBeverageDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.Favorite;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.repository.FavoriteRepository;
import com.sweetbalance.backend.service.BeverageService;
import com.sweetbalance.backend.service.FavoriteService;
import com.sweetbalance.backend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/user")
@Tag(name = "Favorite", description = "즐겨찾기 관련 API")
@RequiredArgsConstructor
public class FavoriteController {

    private final UserService userService;
    private final FavoriteService favoriteService;
    private final BeverageService beverageService;
    private final FavoriteRepository favoriteRepository;

    @Operation(summary = "전체 즐겨찾기 조회")
    @GetMapping("/favorite")
    public ResponseEntity<?> getFavoriteList(@AuthenticationPrincipal UserIdHolder userIdHolder,
                                             @RequestParam("page") int page,
                                             @RequestParam("size") int size,
                                             @RequestParam(value = "sort", defaultValue = "new") String sort) {
        try{

            Long userId = userIdHolder.getUserId();
            Pageable pageable = PageRequest.of(page, size);

            List<FavoriteBeverageDTO> listBeverages = favoriteService.getFavoriteListByUserId(userId, pageable, sort);

            return ResponseEntity.status(200).body(
                    DefaultResponseDTO.success("즐겨찾기 음료 리스트 반환 성공", listBeverages)
            );
        } catch(Exception e){

            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "즐겨찾기 음료 리스트 반환 실패")
            );
        }
    }

    @Operation(summary = "특정 음료에 대한 즐겨찾기 생성")
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

        if (favoriteRepository.findByUserAndBeverage(userOptional.get(), beverageOptional.get()).isPresent()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "해당 음료로 이미 즐겨찾기가 등록되어 있습니다.")
            );
        }

        favoriteService.addFavoriteRecord(userOptional.get(), beverageOptional.get());

        return ResponseEntity.ok(
                DefaultResponseDTO.success("즐겨찾기 추가 성공", null)
        );
    }

    @Operation(summary = "특정 음료에 대한 즐겨찾기 삭제")
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

        Optional<Favorite> favoriteOptional = favoriteRepository.findByUserAndBeverage(userOptional.get(), beverageOptional.get());
        if(favoriteOptional.isEmpty()) {
            return ResponseEntity.status(404).body(
                    DefaultResponseDTO.error(404, 999, "등록된 즐겨찾기 정보를 찾을 수 없습니다.")
            );
        }

        favoriteService.deleteFavoriteRecord(favoriteOptional.get());

        return ResponseEntity.ok(
                DefaultResponseDTO.success("즐겨찾기 삭제 성공", null)
        );
    }
}

package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.response.BeverageDetailDTO;
import com.sweetbalance.backend.service.BeverageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/beverages")
public class BeverageController {

    private final BeverageService beverageService;

    @Autowired
    public BeverageController(BeverageService beverageService) {
        this.beverageService = beverageService;
    }

    @GetMapping("/brand/list")
    public ResponseEntity<?> getBrandList() {
        try {

            List<String> brands = beverageService.getUniqueBrands();
            // List<String> brands = List.of("스타벅스", "메가커피", "빽다방"); // 단순 하드코딩도 가능

            return ResponseEntity.ok(
                    DefaultResponseDTO.success("브랜드 목록 조회 성공", brands)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "브랜드 목록 조회 실패")
            );
        }
    }

    @GetMapping("/brand/popular")
    public ResponseEntity<?> getBrandPopularBeverageList(
            @RequestParam("brand-name") String brandName,
            @RequestParam("top") int top
    ) {
        try {
            List<BeverageDetailDTO> popularBeverages = beverageService.getPopularBeveragesByBrand(brandName, top);
            return ResponseEntity.ok(
                    DefaultResponseDTO.success("인기 음료 조회 성공", popularBeverages)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "인기 음료 조회 실패")
            );
        }
    }
}

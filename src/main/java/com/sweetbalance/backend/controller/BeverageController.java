package com.sweetbalance.backend.controller;

import com.sweetbalance.backend.dto.DefaultResponseDTO;
import com.sweetbalance.backend.dto.response.BeverageDetailsDTO;
import com.sweetbalance.backend.dto.response.BrandPopularBeverageDTO;
import com.sweetbalance.backend.dto.response.InnerListBeverageDTO;
import com.sweetbalance.backend.service.BeverageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            List<BrandPopularBeverageDTO> popularBeverages = beverageService.getPopularBeveragesByBrand(brandName, top);
            return ResponseEntity.ok(
                    DefaultResponseDTO.success("인기 음료 조회 성공", popularBeverages)
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "인기 음료 조회 실패")
            );
        }
    }

    @GetMapping("/{beverage-id}")
    public ResponseEntity<?> getBeverageDetail(@PathVariable("beverage-id") Long beverageId) {
        try {
            BeverageDetailsDTO beverageDetails = beverageService.getBeverageDetails(beverageId);
            return ResponseEntity.ok(
                    DefaultResponseDTO.success("음료 상세 정보 조회 성공", beverageDetails)
            );
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "음료 상세 정보 조회 실패")
            );
        }
    }

    @GetMapping("/list")
    public ResponseEntity<?> getBeverageListFilteredByParameters(
            @RequestParam(value = "brand", required = false) String brand,
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "sort", defaultValue = "lexOrder") String sort
    ) {
        try {
            List<InnerListBeverageDTO> beverages = beverageService.findBeveragesByFilters(
                    brand, category, keyword, sort
            );
            return ResponseEntity.ok(
                    DefaultResponseDTO.success("조건부 음료 리스트 조회 성공", beverages)
            );
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                    DefaultResponseDTO.error(400, 1001, "잘못된 정렬 기준입니다")
            );
        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    DefaultResponseDTO.error(500, 999, "조건부 음료 리스트 조회 실패")
            );
        }
    }

}

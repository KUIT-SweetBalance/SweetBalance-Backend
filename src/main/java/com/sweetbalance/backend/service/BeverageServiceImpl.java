package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.BeverageDetailsDTO;
import com.sweetbalance.backend.dto.response.BeverageSizeDetailsWithRecommendDTO;
import com.sweetbalance.backend.dto.response.BrandPopularBeverageDTO;
import com.sweetbalance.backend.dto.response.RecommendedBeverageDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageSize;
import com.sweetbalance.backend.repository.BeverageRepository;
import com.sweetbalance.backend.repository.BeverageSizeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BeverageServiceImpl implements BeverageService {

    private final BeverageRepository beverageRepository;
    private final BeverageSizeRepository beverageSizeRepository;

    @Autowired
    public BeverageServiceImpl(
            BeverageRepository beverageRepository,
            BeverageSizeRepository beverageSizeRepository) {
        this.beverageRepository = beverageRepository;
        this.beverageSizeRepository = beverageSizeRepository;
    }

    public List<String> getUniqueBrands() {
        return beverageRepository.findDistinctBrands();
    }

    public List<BrandPopularBeverageDTO> getPopularBeveragesByBrand(String brandName, int limit) {
        List<Beverage> beverages = beverageRepository.findTopBeveragesByBrandOrderByConsumeCountDesc(brandName, limit);
        return beverages.stream()
                .map(this::convertToBrandPopularBeverageDTO)
                .collect(Collectors.toList());
    }

    private BrandPopularBeverageDTO convertToBrandPopularBeverageDTO(Beverage beverage) {
        return BrandPopularBeverageDTO.builder()
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .category(beverage.getCategory())
                .consumeCount(beverage.getConsumeCount())
                .build();
    }

    @Override
    public BeverageDetailsDTO getBeverageDetails(Long beverageId) {
        // Fetch the main Beverage entity
        Beverage beverage = beverageRepository.findById(beverageId)
                .orElseThrow(() -> new RuntimeException("Beverage not found"));

        // Map each size to a DTO with recommendations
        List<BeverageSizeDetailsWithRecommendDTO> sizeDetails = beverage.getSizes().stream()
                .map(this::createBeverageSizeDetailsWithRecommend)
                .collect(Collectors.toList());

        // Build and return the response DTO
        return BeverageDetailsDTO.builder()
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .category(beverage.getCategory())
                .consumeCount(beverage.getConsumeCount())
                .sizeDetails(sizeDetails)
                .build();
    }

    private BeverageSizeDetailsWithRecommendDTO createBeverageSizeDetailsWithRecommend(BeverageSize size) {
        // Fetch similar sizes for recommendations
        List<BeverageSize> similarSizes = beverageSizeRepository.findTopSimilarSizesBySugar(
                size.getBeverage().getBeverageId(), size.getSugar(), 5);

        // Convert similar sizes to RecommendedBeverageDTOs
        List<RecommendedBeverageDTO> recommends = similarSizes.stream()
                .map(this::convertToRecommendedBeverageDTO)
                .collect(Collectors.toList());

        // Build and return the size details DTO
        return BeverageSizeDetailsWithRecommendDTO.builder()
                .id(size.getId())
                .sizeType(size.getSizeType())
                .volume(size.getVolume())
                .sugar((int) size.getSugar()) // Cast to int for DTO compatibility
                .calories((int) size.getCalories())
                .caffeine((int) size.getCaffeine())
                .recommends(recommends)
                .build();
    }

    private RecommendedBeverageDTO convertToRecommendedBeverageDTO(BeverageSize size) {
        Beverage beverage = size.getBeverage();
        return RecommendedBeverageDTO.builder()
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .category(beverage.getCategory())
                .consumeCount(beverage.getConsumeCount())
                .beverageSizeId(size.getId())
                .sizeType(size.getSizeType())
                .volume(size.getVolume())
                .sugar((int) size.getSugar()) // Cast to int for DTO compatibility
                .calories((int) size.getCalories())
                .caffeine((int) size.getCaffeine())
                .build();
    }
}

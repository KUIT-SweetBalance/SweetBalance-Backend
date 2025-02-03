package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.BeverageDetailsDTO;
import com.sweetbalance.backend.dto.response.BeverageSizeDetailsWithRecommendDTO;
import com.sweetbalance.backend.dto.response.BrandPopularBeverageDTO;
import com.sweetbalance.backend.dto.response.RecommendedBeverageDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageSize;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.repository.BeverageRepository;
import com.sweetbalance.backend.repository.BeverageSizeRepository;
import com.sweetbalance.backend.util.syrup.Syrup;
import com.sweetbalance.backend.util.syrup.SyrupManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
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

        Beverage beverage = beverageRepository.findById(beverageId)
                .orElseThrow(() -> new RuntimeException("Beverage not found"));

        List<Syrup> syrups = SyrupManager.getSyrupListOfBrand(beverage.getBrand());
        List<String> syrupNames = syrups.stream()
                .map(Syrup::getSyrupName)
                .collect(Collectors.toList());

        List<BeverageSizeDetailsWithRecommendDTO> sizeDetails = beverage.getSizes().stream()
                .map(this::createBeverageSizeDetailsWithRecommend)
                .collect(Collectors.toList());

        return BeverageDetailsDTO.builder()
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .category(beverage.getCategory())
                .consumeCount(beverage.getConsumeCount())
                .syrups(syrupNames) // syrup 리스트의 syrupName 만 String 리스트로 반환하도록 하고 싶어요
                .sizeDetails(sizeDetails)
                .build();
    }

    private BeverageSizeDetailsWithRecommendDTO createBeverageSizeDetailsWithRecommend(BeverageSize size) {

        List<BeverageSize> similarSizes = beverageSizeRepository.findTopSimilarSizesByBrandAndSugar(
                size.getBeverage().getBeverageId(),
                size.getBeverage().getBrand(),
                size.getSugar(),
                5);

        List<RecommendedBeverageDTO> recommends = similarSizes.stream()
                .map(this::convertToRecommendedBeverageDTO)
                .collect(Collectors.toList());

        return BeverageSizeDetailsWithRecommendDTO.builder()
                .id(size.getId())
                .sizeType(size.getSizeType())
                .sizeTypeDetail(size.getSizeTypeDetail())
                .volume(size.getVolume())
                .sugar((int) size.getSugar())
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
                .sizeTypeDetail(size.getSizeTypeDetail())
                .volume(size.getVolume())
                .sugar((int) size.getSugar())
                .calories((int) size.getCalories())
                .caffeine((int) size.getCaffeine())
                .build();
    }

    public Optional<Beverage> findBeverageByBeverageId(Long beverageId) {
        return beverageRepository.findById(beverageId);
    }

}

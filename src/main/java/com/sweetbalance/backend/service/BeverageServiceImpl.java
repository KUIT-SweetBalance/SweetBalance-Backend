package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.BeverageDetailDTO;
import com.sweetbalance.backend.dto.response.BeverageSizeDetailDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageSize;
import com.sweetbalance.backend.repository.BeverageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class BeverageServiceImpl implements BeverageService {

    private final BeverageRepository beverageRepository;

    @Autowired
    public BeverageServiceImpl(BeverageRepository beverageRepository) {
        this.beverageRepository = beverageRepository;
    }

    public List<String> getUniqueBrands() {
        return beverageRepository.findDistinctBrands();
    }

    public List<BeverageDetailDTO> getPopularBeveragesByBrand(String brandName, int limit) {
        List<Beverage> beverages = beverageRepository.findTopBeveragesByBrandOrderByConsumeCountDesc(brandName, limit);
        return beverages.stream()
                .map(this::convertToBeverageListInfoDTO)
                .collect(Collectors.toList());
    }

    private BeverageDetailDTO convertToBeverageListInfoDTO(Beverage beverage) {
        return BeverageDetailDTO.builder()
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .category(beverage.getCategory())
                .sugar(beverage.getSugar())
                .calories(beverage.getCalories())
                .caffeine(beverage.getCaffeine())
                .consumeCount(beverage.getConsumeCount())
                .sizes(beverage.getSizes().stream()
                        .map(size -> convertToBeverageSizeDetailDTO(size, beverage))
                        .collect(Collectors.toList()))
                .build();
    }

    private BeverageSizeDetailDTO convertToBeverageSizeDetailDTO(BeverageSize size, Beverage beverage) {
        double volumeRatio = size.getVolume() / 100.0;

        return BeverageSizeDetailDTO.builder()
                .id(size.getId())
                .sizeType(size.getSizeType())
                .volume(size.getVolume())
                .sugar((int) Math.round(beverage.getSugar() * volumeRatio))
                .calories((int) Math.round(beverage.getCalories() * volumeRatio))
                .caffeine((int) Math.round(beverage.getCaffeine() * volumeRatio))
                .build();
    }
}

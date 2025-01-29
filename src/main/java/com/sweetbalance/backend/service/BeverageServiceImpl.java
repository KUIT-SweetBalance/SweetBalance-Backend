package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.BrandPopularBeverageDTO;
import com.sweetbalance.backend.entity.Beverage;
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
}

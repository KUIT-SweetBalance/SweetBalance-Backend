package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.BeverageDetailsDTO;
import com.sweetbalance.backend.dto.response.BrandPopularBeverageDTO;

import java.util.List;

public interface BeverageService {
    List<String> getUniqueBrands();

    List<BrandPopularBeverageDTO> getPopularBeveragesByBrand(String brandName, int limit);

    BeverageDetailsDTO getBeverageDetails(Long beverageId);
}

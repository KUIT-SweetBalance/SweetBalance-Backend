package com.sweetbalance.backend.service;

import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.dto.response.BeverageDetailsDTO;
import com.sweetbalance.backend.dto.response.BrandPopularBeverageDTO;

import java.util.List;
import java.util.Optional;

public interface BeverageService {
    List<String> getUniqueBrands();

    public Optional<Beverage> findBeverageByBeverageId(Long beverageId);
  
    List<BrandPopularBeverageDTO> getPopularBeveragesByBrand(String brandName, int limit);

    BeverageDetailsDTO getBeverageDetails(Long beverageId);

}

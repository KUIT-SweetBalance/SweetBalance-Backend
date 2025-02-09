package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.InnerListBeverageDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.dto.response.beveragedetail.BeverageDetailsDTO;
import com.sweetbalance.backend.dto.response.BrandPopularBeverageDTO;

import java.util.List;
import java.util.Optional;

public interface BeverageService {

    List<String> getUniqueBrands();

    Optional<Beverage> findBeverageByBeverageId(Long beverageId);
  
    List<BrandPopularBeverageDTO> getPopularBeveragesByBrand(String brandName, int limit);

    BeverageDetailsDTO getBeverageDetails(Long userId, Long beverageId, int limit);

    List<InnerListBeverageDTO> findBeveragesByFilters(Long userId, String brand, String category, String keyword, String sort, Integer page, Integer size);
}

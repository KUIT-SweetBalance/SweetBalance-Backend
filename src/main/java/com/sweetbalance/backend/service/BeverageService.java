package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.BeverageListInfoDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.User;

import java.util.List;
import java.util.Optional;

public interface BeverageService {
    List<String> getUniqueBrands();

    List<BeverageListInfoDTO> getPopularBeveragesByBrand(String brandName, int limit);

    public Optional<Beverage> findBeverageByBeverageId(Long beverageId);
}

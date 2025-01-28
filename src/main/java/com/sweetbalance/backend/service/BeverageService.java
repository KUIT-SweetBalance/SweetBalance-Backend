package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.BeverageListInfoDTO;
import com.sweetbalance.backend.entity.Beverage;

import java.util.List;

public interface BeverageService {
    List<String> getUniqueBrands();

    List<BeverageListInfoDTO> getPopularBeveragesByBrand(String brandName, int limit);
}

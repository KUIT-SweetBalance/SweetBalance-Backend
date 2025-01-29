package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.BeverageDetailDTO;

import java.util.List;

public interface BeverageService {
    List<String> getUniqueBrands();

    List<BeverageDetailDTO> getPopularBeveragesByBrand(String brandName, int limit);
}

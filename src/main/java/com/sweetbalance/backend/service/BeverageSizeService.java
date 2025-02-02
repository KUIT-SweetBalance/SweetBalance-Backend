package com.sweetbalance.backend.service;

import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageSize;

import java.util.Optional;

public interface BeverageSizeService {

    public Optional<BeverageSize> findBeverageSizeByBeverageSizeId(Long beverageSizeId);

}

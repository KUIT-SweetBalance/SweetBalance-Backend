package com.sweetbalance.backend.service;

import com.sweetbalance.backend.entity.BeverageSize;

import java.util.Optional;

public interface BeverageSizeService {

    Optional<BeverageSize> findBeverageSizeByBeverageSizeId(Long beverageSizeId);
}

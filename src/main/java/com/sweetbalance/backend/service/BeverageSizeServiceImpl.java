package com.sweetbalance.backend.service;

import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageSize;
import com.sweetbalance.backend.repository.BeverageSizeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BeverageSizeServiceImpl implements BeverageSizeService {

    private final BeverageSizeRepository beverageSizeRepository;


    @Override
    public Optional<BeverageSize> findBeverageSizeByBeverageAndVolume(Beverage beverage, int volume) {
        return beverageSizeRepository.findByBeverageAndVolume(beverage, volume);
    }
}

package com.sweetbalance.backend.service;

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
    public Optional<BeverageSize> findBeverageSizeByBeverageSizeId(Long beverageSizeId) {
        return beverageSizeRepository.findById(beverageSizeId);
    }
}

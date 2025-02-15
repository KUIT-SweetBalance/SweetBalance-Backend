package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.FavoriteBeverageDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface FavoriteService {
    List<FavoriteBeverageDTO> getFavoriteListByUserId(Long userId, Pageable pageable);
    void addFavoriteRecord(User user, Beverage beverage);
    void deleteFavoriteRecord(User user, Beverage beverage);
}

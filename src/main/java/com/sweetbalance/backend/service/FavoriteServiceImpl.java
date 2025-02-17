package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.FavoriteBeverageDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.Favorite;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.repository.FavoriteRepository;
import com.sweetbalance.backend.util.TimeStringConverter;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class FavoriteServiceImpl implements FavoriteService{

    private final FavoriteRepository favoriteRepository;

    @Override
    public List<FavoriteBeverageDTO> getFavoriteListByUserId(Long userId, Pageable pageable, String sort) {
        List<Favorite> favorites;

        if (sort.equals("old"))
            favorites = favoriteRepository.findByUser_UserIdOrderByCreatedAtAsc(userId, pageable);
        else // new
            favorites = favoriteRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);


        return favorites.stream()
                .map(this::convertToFavoriteBeverageDTO)
                .collect(Collectors.toList());
    }

    private FavoriteBeverageDTO convertToFavoriteBeverageDTO(Favorite favorite) {
        Beverage beverage = favorite.getBeverage();
        return FavoriteBeverageDTO.builder()
                .favoriteId(favorite.getFavoriteId())
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .sugarPer100ml((int) Math.round(beverage.getSugar()))
                .timeString(TimeStringConverter.convertLocalDateTimeToKoreanTimeString(favorite.getCreatedAt()))
                .build();
    }

    @Override
    public void addFavoriteRecord(User user, Beverage beverage) {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setBeverage(beverage);

        favoriteRepository.save(favorite);
    }

    @Override
    public void deleteFavoriteRecord(Favorite favorite) {
        favoriteRepository.delete(favorite);
    }
}

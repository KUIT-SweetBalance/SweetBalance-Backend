package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.dto.response.ListBeverageDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.Favorite;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.repository.FavoriteRepository;
import com.sweetbalance.backend.repository.UserRepository;
import com.sweetbalance.backend.util.TimeStringConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           FavoriteRepository favoriteRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public void join(SignUpRequestDTO signUpRequestDTO){
        // 정규식 처리 등 가입 불가 문자에 대한 처리도 진행해주어야 한다.
        boolean userExists = userRepository.existsByUsername(signUpRequestDTO.getUsername());
        if(userExists) throw new RuntimeException("The username '" + signUpRequestDTO.getUsername() + "' is already taken.");

        User bCryptPasswordEncodedUser = makeBCryptPasswordEncodedUser(signUpRequestDTO);
        userRepository.save(bCryptPasswordEncodedUser);
    }

    public Optional<User> findUserByUserId(Long userId) {
        return userRepository.findById(userId);
    }

    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    // 특정 사용자 ID로 음료 목록 조회
    public List<Beverage> findBeveragesByUserId(Long userId) {
        return userRepository.findBeveragesByUserId(userId);
    }

    private User makeBCryptPasswordEncodedUser(SignUpRequestDTO signUpRequestDTO){
        User user = signUpRequestDTO.toActiveUser();
        String rawPassword = user.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        return user;
    }

    @Override
    public List<ListBeverageDTO> getFavoriteListByUserId(Long userId, Pageable pageable) {
        List<Favorite> favorites = favoriteRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
        return favorites.stream()
                .map(this::convertToListBeverageDTO)
                .collect(Collectors.toList());
    }

    private ListBeverageDTO convertToListBeverageDTO(Favorite favorite) {
        Beverage beverage = favorite.getBeverage();
        return ListBeverageDTO.builder()
                .favoriteId(favorite.getFavoriteId())
                .beverageId(beverage.getBeverageId())
                .name(beverage.getName())
                .brand(beverage.getBrand())
                .imgUrl(beverage.getImgUrl())
                .category(beverage.getCategory())
                .sugar(beverage.getSugar())
                .calories(beverage.getCalories())
                .caffeine(beverage.getCaffeine())
                .timeString(TimeStringConverter.convertLocalDateTimeToKoreanTimeString(favorite.getCreatedAt()))
                .build();
    }
}
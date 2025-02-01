package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.dto.response.ListBeverageDTO;
import com.sweetbalance.backend.dto.response.WeeklyInfoDTO;
import com.sweetbalance.backend.entity.User;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserService {

    public void join(SignUpRequestDTO signUpRequestDTO);

    public Optional<User> findUserByUserId(Long userId);

    public Optional<User> findUserByUsername(String username);

    List<ListBeverageDTO> getFavoriteListByUserId(Long userId, Pageable pageable);

    WeeklyInfoDTO getWeeklyConsumeInfo(Long userId, LocalDate startDate, LocalDate endDate);
}

package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.dto.response.ListBeverageDTO;
import com.sweetbalance.backend.dto.response.WeeklyInfoDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.entity.BeverageSize;
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

    public Optional<BeverageLog> findBeverageLogByBeverageLogId(Long beverageLogId);

    public void updateMetaData(User user, MetadataRequestDTO metaDataRequestDTO);

    public void addBeverageRecord(User user, BeverageSize beverageSize, AddBeverageRecordRequestDTO addBeverageRecordRequestDTO);

    public void deleteBeverageRecord(BeverageLog beverageLog);

    public void addFavoriteRecord(User user, Beverage beverage);

    public void deleteFavoriteRecord(User user, Beverage beverage);
}

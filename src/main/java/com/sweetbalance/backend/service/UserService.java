package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.dto.response.FavoriteBeverageDTO;
import com.sweetbalance.backend.dto.response.WeeklyInfoDTO;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.entity.BeverageSize;
import com.sweetbalance.backend.entity.User;
import com.sweetbalance.backend.enums.user.LoginType;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface UserService {

    public void join(SignUpRequestDTO signUpRequestDTO);

    public Optional<User> findUserByUserId(Long userId);

    public Optional<User> findUserByEmailAndLoginType(String email, LoginType loginType);

    List<FavoriteBeverageDTO> getFavoriteListByUserId(Long userId, Pageable pageable);

    WeeklyInfoDTO getWeeklyConsumeInfo(Long userId, LocalDate startDate, LocalDate endDate);

    public Optional<BeverageLog> findBeverageLogByBeverageLogId(Long beverageLogId);

    public void updateMetaData(User user, MetadataRequestDTO metaDataRequestDTO);

    public void addBeverageRecord(User user, BeverageSize beverageSize, AddBeverageRecordRequestDTO addBeverageRecordRequestDTO);

    public void deleteBeverageRecord(BeverageLog beverageLog);

    public void addFavoriteRecord(User user, Beverage beverage);

    public void deleteFavoriteRecord(User user, Beverage beverage);

    public void editBeverageRecord(Long beverageLogId, BeverageSize beverageSize, AddBeverageRecordRequestDTO dto);

    public List<BeverageLog> findTodayBeverageLogsByUserId(Long userId);

    List<BeverageLog> findTotalBeverageLogsByUserId(Long userId, Pageable pageable);
}

package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.dto.response.FavoriteBeverageDTO;
import com.sweetbalance.backend.dto.response.notice.ListNoticeDTO;
import com.sweetbalance.backend.dto.response.weekly.WeeklyConsumeInfoDTO;
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

    // user date
    void join(SignUpRequestDTO signUpRequestDTO);
    void updateMetaData(User user, MetadataRequestDTO metaDataRequestDTO);
    boolean resetPassword(String email, String newPassword);
    void softDeleteUser(User user);

    Optional<User> findUserByUserId(Long userId);
    Optional<User> findUserByEmailAndLoginTypeAndDeletedAtIsNull(String email, LoginType loginType);

    // beverage log
    void addBeverageRecord(User user, BeverageSize beverageSize, AddBeverageRecordRequestDTO addBeverageRecordRequestDTO);
    void editBeverageRecord(Long beverageLogId, BeverageSize beverageSize, AddBeverageRecordRequestDTO dto);
    void deleteBeverageRecord(BeverageLog beverageLog);

    Optional<BeverageLog> findBeverageLogByBeverageLogId(Long beverageLogId);

    List<BeverageLog> findTodayBeverageLogsByUserId(Long userId);
    List<BeverageLog> findTotalBeverageLogsByUserId(Long userId, Pageable pageable);

    WeeklyConsumeInfoDTO getWeeklyConsumeInfo(Long userId, LocalDate startDate, LocalDate endDate);

    // favorite
    List<FavoriteBeverageDTO> getFavoriteListByUserId(Long userId, Pageable pageable);
    void addFavoriteRecord(User user, Beverage beverage);
    void deleteFavoriteRecord(User user, Beverage beverage);

    // notice
    List<ListNoticeDTO> getNoticeListByUserId(Long userId);
    void checkNoticeReaded(Long beverageLogId);
    int getNumberOfUnreadLogWithinAWeek();
    
    // email
    boolean sendEmailVerificationCode(String email);
    boolean checkEmailVerificationCode(String email, String code);
}

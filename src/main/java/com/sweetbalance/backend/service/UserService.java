package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.dto.response.FavoriteBeverageDTO;
import com.sweetbalance.backend.dto.response.notice.EachEntry;
import com.sweetbalance.backend.dto.response.notice.ListNoticeDTO;
import com.sweetbalance.backend.dto.response.WeeklyConsumeInfoDTO;
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

    Optional<User> findUserByEmailAndLoginTypeAndDeletedAtIsNull(String email, LoginType loginType);

    void softDeleteUser(User user);

    List<FavoriteBeverageDTO> getFavoriteListByUserId(Long userId, Pageable pageable);

    WeeklyConsumeInfoDTO getWeeklyConsumeInfo(Long userId, LocalDate startDate, LocalDate endDate);

    public Optional<BeverageLog> findBeverageLogByBeverageLogId(Long beverageLogId);

    public void updateMetaData(User user, MetadataRequestDTO metaDataRequestDTO);

    public void addBeverageRecord(User user, BeverageSize beverageSize, AddBeverageRecordRequestDTO addBeverageRecordRequestDTO);

    public void deleteBeverageRecord(BeverageLog beverageLog);

    public List<ListNoticeDTO> getNoticeListByUserId(Long userId);

    public void checkNoticeReaded(Long beverageLogId);

    public void addFavoriteRecord(User user, Beverage beverage);

    public void deleteFavoriteRecord(User user, Beverage beverage);

    public void editBeverageRecord(Long beverageLogId, BeverageSize beverageSize, AddBeverageRecordRequestDTO dto);

    public List<BeverageLog> findTodayBeverageLogsByUserId(Long userId);

    List<BeverageLog> findTotalBeverageLogsByUserId(Long userId, Pageable pageable);

    int getNumberOfUnreadLogWithinAWeek();

    public boolean sendTemporaryPassword(String email);

    public boolean sendEmailVerificationCode(String email);

    public boolean checkEmailVerificationCode(String email, String code);

    public boolean resetPassword(String email, String newPassword);
}

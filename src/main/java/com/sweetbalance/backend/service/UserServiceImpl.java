package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.dto.response.DailySugarDTO;
import com.sweetbalance.backend.dto.response.ListBeverageDTO;
import com.sweetbalance.backend.dto.response.WeeklyInfoDTO;

import com.sweetbalance.backend.entity.*;
import com.sweetbalance.backend.enums.common.Status;

import com.sweetbalance.backend.repository.BeverageLogRepository;
import com.sweetbalance.backend.repository.FavoriteRepository;
import com.sweetbalance.backend.repository.UserRepository;

import com.sweetbalance.backend.util.TimeStringConverter;
import com.sweetbalance.backend.util.SyrupToSugarMapper;
import org.springframework.data.domain.Pageable;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final BeverageLogRepository beverageLogRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UserServiceImpl(UserRepository userRepository,
                           FavoriteRepository favoriteRepository,
                           BeverageLogRepository beverageLogRepository,
                           BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.favoriteRepository = favoriteRepository;
        this.beverageLogRepository = beverageLogRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @Override
    public void join(SignUpRequestDTO signUpRequestDTO){
        // 정규식 처리 등 가입 불가 문자에 대한 처리도 진행해주어야 한다.
        boolean userExists = userRepository.existsByUsername(signUpRequestDTO.getUsername());
        if(userExists) throw new RuntimeException("The username '" + signUpRequestDTO.getUsername() + "' is already taken.");

        User bCryptPasswordEncodedUser = makeBCryptPasswordEncodedUser(signUpRequestDTO);
        userRepository.save(bCryptPasswordEncodedUser);
    }
  
    private User makeBCryptPasswordEncodedUser(SignUpRequestDTO signUpRequestDTO){
        User user = signUpRequestDTO.toActiveUser();
        String rawPassword = user.getPassword();
        String encodedPassword = bCryptPasswordEncoder.encode(rawPassword);
        user.setPassword(encodedPassword);
        return user;
    }

    @Override
    public Optional<User> findUserByUserId(Long userId) {
        return userRepository.findById(userId);
    }

    @Override
    public Optional<User> findUserByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<BeverageLog> findBeverageLogByBeverageLogId(Long beverageLogId) {
        return beverageLogRepository.findById(beverageLogId);
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

    @Override
    public WeeklyInfoDTO getWeeklyConsumeInfo(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveEndDate = endDate.isAfter(today) ? today : endDate;

        List<BeverageLog> logs = beverageLogRepository.findByUser_UserIdAndCreatedAtBetween(
                userId,
                startDate.atStartOfDay(),
                effectiveEndDate.atTime(23, 59, 59)
        );

        int intake = logs.size();
        double totalSugar = logs.stream()
                .mapToDouble(log -> log.getBeverageSize().getSugar() + log.getAdditionalSugar())
                .sum();
        double totalCalories = logs.stream()
                .mapToDouble(log -> log.getBeverageSize().getCalories())
                .sum();

        Map<LocalDate, Double> dailySugar = logs.stream()
                .collect(Collectors.groupingBy(
                        log -> log.getCreatedAt().toLocalDate(),
                        Collectors.summingDouble(log -> log.getBeverageSize().getSugar() + log.getAdditionalSugar())
                ));

        long effectiveDays = ChronoUnit.DAYS.between(startDate, effectiveEndDate) + 1;
        double averageSugar = totalSugar / effectiveDays;

        List<DailySugarDTO> dailySugarList = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(effectiveEndDate); date = date.plusDays(1)) {
            dailySugarList.add(new DailySugarDTO(date, dailySugar.getOrDefault(date, 0.0)));
        }

        return new WeeklyInfoDTO(intake, totalSugar, averageSugar, totalCalories, dailySugarList);
    }
  
    @Override
    public void updateMetaData(User user, MetadataRequestDTO metaDataRequestDTO) {
        user.setGender(metaDataRequestDTO.getGender());
        user.setNickname(metaDataRequestDTO.getNickname());
        // user에 one_liner 추가하면 주석 해제
        // user.setOne_liner(metaDataRequestDTO.getOne_liner());
        userRepository.save(user);
    }

    @Override
    public void addBeverageRecord(User user, BeverageSize beverageSize, AddBeverageRecordRequestDTO dto) {
        BeverageLog beverageLog = new BeverageLog();
        beverageLog.setUser(user);
        beverageLog.setBeverageSize(beverageSize);
        beverageLog.setSyrupName(dto.getSyrupName());
        beverageLog.setSyrupCount(dto.getSyrupCount());
        beverageLog.setStatus(Status.ACTIVE);
        double additionalSugar = (dto.getSyrupName() == null) ? 
                0D : dto.getSyrupCount() * SyrupToSugarMapper.getAmountOfSugar(dto.getSyrupName());
        beverageLog.setAdditionalSugar(additionalSugar);

        beverageLogRepository.save(beverageLog);
    }

    // 로그 Id로 음료 기록 찾고, 음료 사이즈 정보, 시럽 이름, 시럽 개수, 추가 당 함량 다시 설정.
    @Override
    public void editBeverageRecord(Long beverageLogId, BeverageSize beverageSize, AddBeverageRecordRequestDTO dto) {
        Optional<BeverageLog> log = beverageLogRepository.findById(beverageLogId);
        BeverageLog beverageLog = log.get();
        beverageLog.setBeverageSize(beverageSize);
        beverageLog.setSyrupName(dto.getSyrupName());
        beverageLog.setSyrupCount(dto.getSyrupCount());
        double additionalSugar = (dto.getSyrupName() == null) ?
                0D : dto.getSyrupCount() * SyrupToSugarMapper.getAmountOfSugar(dto.getSyrupName());
        beverageLog.setAdditionalSugar(additionalSugar);
        beverageLogRepository.save(beverageLog);
    }

    @Override
    public void deleteBeverageRecord(BeverageLog beverageLog) {
        beverageLog.setStatus(Status.DELETED);
        beverageLogRepository.save(beverageLog);
    }

    @Override
    public void addFavoriteRecord(User user, Beverage beverage) {
        Favorite favorite = new Favorite();
        favorite.setUser(user);
        favorite.setBeverage(beverage);

        favoriteRepository.save(favorite);
    }

    @Override
    public void deleteFavoriteRecord(User user, Beverage beverage) {
        Optional<Favorite> favoriteOptional = favoriteRepository.findByUserAndBeverage(user, beverage);

        favoriteOptional.ifPresent(favoriteRepository::delete);
    }
}
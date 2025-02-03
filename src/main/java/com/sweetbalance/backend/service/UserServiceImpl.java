package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.dto.response.DailySugarDTO;
import com.sweetbalance.backend.dto.response.ListBeverageDTO;
import com.sweetbalance.backend.dto.response.WeeklyInfoDTO;

import com.sweetbalance.backend.entity.*;
import com.sweetbalance.backend.enums.common.Status;

import com.sweetbalance.backend.repository.*;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.sweetbalance.backend.util.TimeStringConverter;
import com.sweetbalance.backend.util.syrup.SugarCalculator;
import com.sweetbalance.backend.util.syrup.SyrupManager;
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
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final BeverageSizeRepository beverageSizeRepository;
    private final BeverageRepository beverageRepository;
    private final SugarCalculator sugarCalculator;
    private final BeverageLogRepository beverageLogRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

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
        userRepository.save(user);
    }

    @Override
    public void addBeverageRecord(User user, BeverageSize beverageSize, AddBeverageRecordRequestDTO dto) {

        Beverage beverage = beverageSizeRepository.findById(beverageSize.getId())
                .map(BeverageSize::getBeverage)
                .orElseThrow(() -> new EntityNotFoundException("Beverage not found for id: " + beverageSize.getId()));

        double additionalSugar = sugarCalculator.calculate(beverage.getBrand(), dto.getSyrupName(), dto.getSyrupCount());

        BeverageLog beverageLog = BeverageLog.builder()
                .user(user)
                .beverageSize(beverageSize)
                .syrupName(dto.getSyrupName())
                .syrupCount(dto.getSyrupCount())
                .additionalSugar(additionalSugar)
                .status(Status.ACTIVE)
                .build();

        beverageLogRepository.save(beverageLog);

        addConsumeCount(beverage);

    }

    // 로그 Id로 음료 기록 찾고, 음료 사이즈 정보, 시럽 이름, 시럽 개수, 추가 당 함량 다시 설정.
    @Override
    public void editBeverageRecord(Long beverageLogId, BeverageSize newBeverageSize, AddBeverageRecordRequestDTO dto) {

        BeverageLog beverageLog = beverageLogRepository.findById(beverageLogId)
                .orElseThrow(() -> new EntityNotFoundException("BeverageLog not found for id: " + beverageLogId));

        Beverage originalBeverage = beverageLog.getBeverageSize().getBeverage();
        Beverage newBeverage = beverageSizeRepository.findById(newBeverageSize.getId())
                .map(BeverageSize::getBeverage)
                .orElseThrow(() -> new EntityNotFoundException("Beverage not found for id: " + newBeverageSize.getId()));

        boolean isBeverageChanged = !originalBeverage.getBeverageId().equals(newBeverage.getBeverageId());

        double additionalSugar = sugarCalculator.calculate(newBeverage.getBrand(), dto.getSyrupName(), dto.getSyrupCount());

        beverageLog.updateRecord(newBeverageSize, dto.getSyrupName(),dto.getSyrupCount(),additionalSugar);
        beverageLogRepository.save(beverageLog);

        if(isBeverageChanged){
            subConsumeCount(originalBeverage);
            addConsumeCount(newBeverage);
        }

    }

    @Override
    public void deleteBeverageRecord(BeverageLog beverageLog) {
        beverageLog.markDeleted();
        beverageLogRepository.save(beverageLog);

        Beverage beverage = beverageLog.getBeverageSize().getBeverage();
        subConsumeCount(beverage);
    }

    private void addConsumeCount(Beverage beverage){
        beverage.setConsumeCount(beverage.getConsumeCount() + 1);
        beverageRepository.save(beverage);
    }

    private void subConsumeCount(Beverage beverage){
        beverage.setConsumeCount(beverage.getConsumeCount() - 1);
        beverageRepository.save(beverage);
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
package com.sweetbalance.backend.service;

import com.nimbusds.jose.util.Pair;
import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.dto.request.MetadataRequestDTO;
import com.sweetbalance.backend.dto.request.SignUpRequestDTO;
import com.sweetbalance.backend.dto.response.DailySugarDTO;
import com.sweetbalance.backend.dto.response.ListNoticeDTO;
import com.sweetbalance.backend.dto.response.FavoriteBeverageDTO;
import com.sweetbalance.backend.dto.response.WeeklyConsumeInfoDTO;

import com.sweetbalance.backend.entity.*;
import com.sweetbalance.backend.enums.alarm.SugarWarningMessage;
import com.sweetbalance.backend.enums.common.Status;

import com.sweetbalance.backend.enums.user.LoginType;
import com.sweetbalance.backend.repository.*;
import com.sweetbalance.backend.util.syrup.SugarCalculator;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

import com.sweetbalance.backend.util.TimeStringConverter;

import org.springframework.data.domain.Pageable;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FavoriteRepository favoriteRepository;
    private final BeverageSizeRepository beverageSizeRepository;
    private final BeverageRepository beverageRepository;
    private final AlarmRepository alarmRepository;
    private final SugarCalculator sugarCalculator;
    private final BeverageLogRepository beverageLogRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    @Override
    public void join(SignUpRequestDTO signUpRequestDTO){
        // 정규식 처리 등 가입 불가 문자에 대한 처리도 진행해주어야 한다.
        boolean userExists = userRepository.existsByEmailAndLoginType(signUpRequestDTO.getEmail(), LoginType.BASIC);
        if(userExists) throw new RuntimeException("The email '" + signUpRequestDTO.getEmail() + "' is already taken.");

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
    public Optional<User> findUserByEmailAndLoginType(String email, LoginType loginType) {
        return userRepository.findByEmailAndLoginType(email, loginType);
    }

    @Override
    public Optional<BeverageLog> findBeverageLogByBeverageLogId(Long beverageLogId) {
        return beverageLogRepository.findById(beverageLogId);
    }

    @Override
    public List<FavoriteBeverageDTO> getFavoriteListByUserId(Long userId, Pageable pageable) {
        List<Favorite> favorites = favoriteRepository.findByUser_UserIdOrderByCreatedAtDesc(userId, pageable);
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
                .sugar((int) Math.round(beverage.getSugar()))
                .timeString(TimeStringConverter.convertLocalDateTimeToKoreanTimeString(favorite.getCreatedAt()))
                .build();
    }

    @Override
    public WeeklyConsumeInfoDTO getWeeklyConsumeInfo(Long userId, LocalDate startDate, LocalDate endDate) {
        LocalDate today = LocalDate.now();
        LocalDate effectiveEndDate = endDate.isAfter(today) ? today : endDate;

        List<BeverageLog> logs = beverageLogRepository.findByUser_UserIdAndCreatedAtBetweenAndStatus(
                userId,
                startDate.atStartOfDay(),
                effectiveEndDate.atTime(23, 59, 59),
                Status.ACTIVE
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
            dailySugarList.add(new DailySugarDTO(date, Math.round(dailySugar.getOrDefault(date, 0.0) * 10.0) / 10.0));
        }

        return WeeklyConsumeInfoDTO.builder()
                .intake(intake)
                .totalSugar((int) Math.round(totalSugar))
                .averageSugar(Math.round(averageSugar * 10.0) / 10.0)
                .totalCalories((int) Math.round(totalCalories))
                .unreadAlarmCount(getNumberOfUnreadLogWithinAWeek())
                .dailySugar(dailySugarList)
                .build();
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
                .readByUser(false)
                .build();

        beverageLogRepository.save(beverageLog);

        addConsumeCount(beverage);

        updateAlarm(user, beverageLog);

    }

    public void updateAlarm(User user, BeverageLog beverageLog){
        List<BeverageLog> logsOnSameDay = getLogsOnSameDay(user.getUserId(), beverageLog);
        List<Alarm> alarmsOnSameDay = getAlarmsOnSameDay(user.getUserId(), beverageLog);
        System.out.println("logsOnSameDay = " + logsOnSameDay);
        System.out.println("alarmsOnSameDay = " + alarmsOnSameDay);

        // 알람의 위치는 어디가 적절한가?
        List<Pair<Long,SugarWarningMessage>> properAlarm = getproperAlarm(logsOnSameDay);
        System.out.println("properAlarm = " + properAlarm);

        int size1 = alarmsOnSameDay.size();
        int size2 = properAlarm.size();
        int max_size = Math.max(size1, size2);

        for (int i = 0; i < max_size; i++) {
            Long val1 = (i < size1) ? alarmsOnSameDay.get(i).getId() : null;
            Long val2 = (i < size2) ? properAlarm.get(i).getLeft() : null;

            // case 1) 둘 다 존재할 때
            if (val1 != null && val2 != null) {
                if (!val1.equals(val2)) {
                    // 인덱스가 같지만 값이 다름
                    // val1의 로그 id를 val2로 변경
                    Alarm alarm = alarmRepository.findById(alarmsOnSameDay.get(i).getId()).orElseThrow();
                    alarm.setLog(beverageLogRepository.findById(val2).get());
                    alarmRepository.save(alarm);
                }
            }
            // case 2) 한쪽은 이미 끝까지 간 경우 (null)
            else {
                // 한쪽 리스트에만 남은 원소가 존재
                if (val1 != null) {
                    // alarmsOnSameDay에만 값이 남아 있을 때
                    Alarm alarm = alarmRepository.findById(alarmsOnSameDay.get(i).getId()).orElseThrow();
                    alarmRepository.delete(alarm);
                }
                if (val2 != null) {
                    // appropriatePositions에만 값이 남아 있을 때
                    String warningMessage;
                    if(i == 1){
                        warningMessage = "당 25g 이상 섭취, 일일 권장량 초과";
                    } else {
                        warningMessage = "당 20g 섭취, 주의 필요";
                    }
                    Alarm alarm = Alarm.of(beverageLogRepository.findById(val2).get(), properAlarm.get(i).getRight().getMessage());
                    System.out.println("alarm = " + alarm);
                    alarmRepository.save(alarm);
                }
            }
        }


    }

    private List<Pair<Long,SugarWarningMessage>> getproperAlarm(List<BeverageLog> logsOnSameDay) {
        final double cautionAmountOfSugar = 20.0D;
        final double exceedAmountOfSugar = 25.0D;

        List<Pair<Long,SugarWarningMessage>> properAlarm = new ArrayList<>();
        double accumulatedSugar = 0.0D;
        double creteria = cautionAmountOfSugar;
        for (BeverageLog bl : logsOnSameDay) {
            accumulatedSugar += bl.getBeverageSize().getSugar() + bl.getAdditionalSugar();
            if(accumulatedSugar >= exceedAmountOfSugar){
                properAlarm.add(Pair.of(bl.getLogId(), SugarWarningMessage.EXCEED));
                break;
            } else if (accumulatedSugar >= creteria){
                properAlarm.add(Pair.of(bl.getLogId(), SugarWarningMessage.CAUTION));
                creteria = exceedAmountOfSugar;
            }
        }
        return properAlarm;
    }

    public List<BeverageLog> getLogsOnSameDay(Long userId, BeverageLog beverageLog) {
        LocalDate updatedDate = beverageLog.getUpdatedAt().toLocalDate();
        LocalDateTime startOfDay = updatedDate.atStartOfDay();
        LocalDateTime endOfDay = updatedDate.plusDays(1).atStartOfDay().minusNanos(1);
        return beverageLogRepository.findAllByUserUserIdAndStatusAndUpdatedAtBetween(userId, Status.ACTIVE, startOfDay,endOfDay);
    }

    public List<Alarm> getAlarmsOnSameDay(Long userId, BeverageLog beverageLog) {
        LocalDate updatedDate = beverageLog.getUpdatedAt().toLocalDate();
        LocalDateTime startOfDay = updatedDate.atStartOfDay();
        LocalDateTime endOfDay = updatedDate.plusDays(1).atStartOfDay().minusNanos(1);
        return alarmRepository.findAllByLogUserUserIdAndUpdatedAtBetween(userId,startOfDay,endOfDay);
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

        User user = beverageLog.getUser();
        updateAlarm(user, beverageLog);

    }

    @Override
    public void deleteBeverageRecord(BeverageLog beverageLog) {
        beverageLog.markDeleted();
        beverageLogRepository.save(beverageLog);

        Beverage beverage = beverageLog.getBeverageSize().getBeverage();
        subConsumeCount(beverage);

        User user = beverageLog.getUser();
        updateAlarm(user, beverageLog);
    }

    @Override
    public List<ListNoticeDTO> getNoticeListByUserId(Long userId) {
        Long start, end;
        double ms;
        start = System.nanoTime();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);

        List<Alarm> allAlarms = alarmRepository
                .findAllByLogUserUserIdAndCreatedAtBetween(userId, oneWeekAgo, now);

        Map<Long, Alarm> alarmByLogId = allAlarms.stream().collect(Collectors.toMap(alarm -> alarm.getLog().getLogId(), Function.identity()));

        List<BeverageLog> sortedLogs = beverageLogRepository
                .findAllByUserUserIdAndStatusAndCreatedAtBetween(userId,Status.ACTIVE,oneWeekAgo,now)
                .stream()
                .sorted(Comparator.comparing(BeverageLog::getCreatedAt))
                .collect(Collectors.toList());


        List<BaseEntity> integratedLogs = new ArrayList<>();

        for(BeverageLog log: sortedLogs){
            integratedLogs.add(log);

            Alarm alarm = alarmByLogId.get(log.getLogId());
            if(alarm != null){
                integratedLogs.add(alarm);
            }
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        start = System.nanoTime();
        List<ListNoticeDTO> result = integratedLogs
                .stream()
                .map(entity -> {
            String timeString = entity.getCreatedAt().format(formatter);

            String message;


            if(entity instanceof BeverageLog log){
                Beverage beverage =  log.getBeverageSize().getBeverage();
                message = beverage.getBrand() + " " +  beverage.getName();

                Map<String, Object> beverageLogInfo = new LinkedHashMap<>();
                beverageLogInfo.put("image",beverage.getImgUrl());
                beverageLogInfo.put("sugar",beverage.getSugar());
                beverageLogInfo.put("syrupName",log.getSyrupName());
                beverageLogInfo.put("syrupCount",log.getSyrupCount());
                beverageLogInfo.put("size",log.getBeverageSize().getSizeType());
                beverageLogInfo.put("beverageLogId",log.getLogId());
                beverageLogInfo.put("isReaded",log.getReadByUser());

                return new ListNoticeDTO(timeString,message,beverageLogInfo);
            } else if (entity instanceof Alarm alarm){
                message = alarm.getContent();
                return  new ListNoticeDTO(timeString,message,null);
            } else {
                throw new IllegalStateException("알 수 없는 타입: " + entity.getClass());
            }

        }).toList().reversed();

        end = System.nanoTime();
        ms = (end - start) / (1000 * 1000D);
        System.out.println(ms + " ms");

        return result;
    }

    @Override
    public void checkNoticeReaded(Long beverageLogId) {
        BeverageLog beverageLog = beverageLogRepository
                .findById(beverageLogId)
                .orElseThrow(() -> new EntityNotFoundException("일치하는 음료기록을 찾을 수 없습니다."));

        beverageLog.setReadByUser(true);
        beverageLogRepository.save(beverageLog);
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

    @Override
    public List<BeverageLog> findTodayBeverageLogsByUserId(Long userId) {
        if (!userRepository.existsById(userId)) {
            return Collections.emptyList();
        }

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay().minusNanos(1);

        //findAllByUserUserIdAndCreatedAtBetween
        return beverageLogRepository.findByUser_UserIdAndCreatedAtBetween(
                userId, startOfToday, endOfToday
        );
    }

    @Override
    public List<BeverageLog> findTotalBeverageLogsByUserId(Long userId, Pageable pageable) {
        return beverageLogRepository.findTotalByUserUserId(userId, pageable);
    }

    @Override
    public int getNumberOfUnreadLogWithinAWeek() {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        return beverageLogRepository
                .findByCreatedAtAfterAndReadByUserFalse(sevenDaysAgo)
                .size();
    }
}
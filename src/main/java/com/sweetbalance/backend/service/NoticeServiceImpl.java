package com.sweetbalance.backend.service;

import com.nimbusds.jose.util.Pair;
import com.sweetbalance.backend.dto.response.notice.EachEntry;
import com.sweetbalance.backend.dto.response.notice.ListNoticeDTO;
import com.sweetbalance.backend.entity.*;
import com.sweetbalance.backend.enums.alarm.SugarWarning;
import com.sweetbalance.backend.enums.common.Status;
import com.sweetbalance.backend.enums.user.Gender;
import com.sweetbalance.backend.repository.AlarmRepository;
import com.sweetbalance.backend.repository.BeverageLogRepository;
import com.sweetbalance.backend.util.notice.AlarmMessage;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class NoticeServiceImpl implements NoticeService{

    private final AlarmRepository alarmRepository;
    private final BeverageLogRepository beverageLogRepository;

    @Override
    public List<ListNoticeDTO> getNoticeListByUserId(Long userId) {
        Long start, end;
        double ms;
        start = System.nanoTime();

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1).toLocalDate().atStartOfDay();

        List<Alarm> allAlarms = alarmRepository
                .findAllByLogUserUserIdAndCreatedAtBetween(userId, oneWeekAgo, now);
        Map<Long, Alarm> alarmByLogId = allAlarms.stream().collect(Collectors.toMap(alarm -> alarm.getLog().getLogId(), Function.identity()));

        List<BeverageLog> sortedLogs = beverageLogRepository
                .findAllByUserUserIdAndStatusAndCreatedAtBetween(userId, Status.ACTIVE,oneWeekAgo,now)
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

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        start = System.nanoTime();
        List<EachEntry> flatList = integratedLogs
                .stream()
                .map(entity -> {

                    String timeString = entity.getCreatedAt().format(dateTimeFormatter);
                    String message;

                    switch (entity) {
                        case BeverageLog log -> {
                            Beverage beverage =  log.getBeverageSize().getBeverage();
                            message = beverage.getBrand() + " " +  beverage.getName();

                            Map<String, Object> beverageLogInfo = new LinkedHashMap<>();
                            beverageLogInfo.put("image",beverage.getImgUrl());
                            beverageLogInfo.put("sugar",(int)Math.ceil(beverage.getSugar()));
                            beverageLogInfo.put("syrupName",log.getSyrupName());
                            beverageLogInfo.put("syrupCount",log.getSyrupCount());
                            beverageLogInfo.put("size",log.getBeverageSize().getSizeType());
                            beverageLogInfo.put("beverageLogId",log.getLogId());
                            beverageLogInfo.put("isRead",log.getReadByUser());

                            return new EachEntry(timeString,message,beverageLogInfo);
                        }

                        case Alarm alarm -> {
                            message = alarm.getContent();
                            return  new EachEntry(timeString,message,null);
                        }

                        default -> throw new IllegalStateException("알 수 없는 타입: " + entity.getClass());
                    }

                }).toList().reversed();

        Map<String, List<EachEntry>> groupedMap = new LinkedHashMap<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        for (EachEntry dto : flatList) {
            // (1) timeString 파싱 → LocalDateTime
            LocalDateTime parsed = LocalDateTime.parse(dto.timeString(), dateTimeFormatter);

            // (2) 날짜 문자열 만들기: "yyyy.MM.dd"
            String dateKey = parsed.format(dateFormatter);

            // (3) dto의 timeString(시각 부분)도 "HH:mm"으로 변경하고 싶다면, 새 DTO를 만들거나 수정
            String onlyTime = parsed.format(timeFormatter);

            // beverageLogInfo는 그대로 쓰고, message도 그대로 쓰되 timeString만 교체
            EachEntry newDto = new EachEntry(
                    onlyTime,             // "HH:mm"
                    dto.message(),
                    dto.beverageLogInfo()
            );

            // (4) dateKey로 groupedMap에 put
            groupedMap.computeIfAbsent(dateKey, k -> new ArrayList<>())
                    .add(newDto);
        }

        // 2) groupedMap을 최종 List<DayGroupedDTO>로 변환
        List<ListNoticeDTO> result = new ArrayList<>();
        for (Map.Entry<String, List<EachEntry>> entry : groupedMap.entrySet()) {
            String date = entry.getKey();
            List<EachEntry> infoList = entry.getValue();

            // 하나의 날짜에 대한 DTO 생성
            ListNoticeDTO dayDTO = new ListNoticeDTO(date, infoList);
            result.add(dayDTO);
        }
        result.sort(Comparator.comparing(ListNoticeDTO::date));


        end = System.nanoTime();
        ms = (end - start) / (1000 * 1000D);
        System.out.println(ms + " ms");

        return result.reversed();
    }

    @Override
    public void checkNoticeReaded(Long beverageLogId) {
        BeverageLog beverageLog = beverageLogRepository
                .findById(beverageLogId)
                .orElseThrow(() -> new EntityNotFoundException("일치하는 음료기록을 찾을 수 없습니다."));

        beverageLog.setReadByUser(true);
        beverageLogRepository.save(beverageLog);
    }


    public void updateAlarm(User user, BeverageLog beverageLog){
        List<BeverageLog> logsOnSameDay = getLogsOnSameDay(user.getUserId(), beverageLog);
        List<Alarm> alarmsOnSameDay = getAlarmsOnSameDay(user.getUserId(), beverageLog);

        // 성별에 따라 다른 당 섭취 기준 적용
        Gender gender = user.getGender();
        final int cautionAmountOfSugar = (gender == Gender.MALE) ? 33 : 20;
        final int exceedAmountOfSugar = (gender == Gender.MALE) ? 38 : 25;

        // 알람의 위치는 어디가 적절한가?
        List<Pair<Long, SugarWarning>> properAlarmList = getProperAlarmLocation(user.getGender(), logsOnSameDay, (double)cautionAmountOfSugar, (double)exceedAmountOfSugar);
        System.out.println("alarmsOnSameDay: ");
        alarmsOnSameDay.forEach(a -> System.out.println(a.getLog().getLogId()));
        System.out.println("properAlarmList: ");
        properAlarmList.forEach(p -> System.out.println(p.getLeft() +"," + p.getRight()));
        /*
        cases:
        1.  alarms: 두개
            proper: none
        2.  alarms: 두개
            proper: 하나(위치 바뀜)
        3.  alarms: 하나
            proper: 하나(위치 바뀜)
        4.  alarms: 하나
            proper: 두개
            ..... 계속
         */


        int size1 = alarmsOnSameDay.size();
        int size2 = properAlarmList.size();
        int max_size = Math.max(size1, size2);

        for (int i = 0; i < max_size; i++) {
            // 알람 엔티티
            Alarm existingAlarm = (i < size1) ? alarmsOnSameDay.get(i) : null;
            // 로그 Id
            Long properLocationLogId = (i < size2) ? properAlarmList.get(i).getLeft() : null;

            // case 1) 둘 다 존재할 때
            if (existingAlarm != null && properLocationLogId != null) {
                Long logIdOfExistingAlarm = existingAlarm.getLog().getLogId();

                if (!logIdOfExistingAlarm.equals(properLocationLogId)) {
                    // 인덱스가 같지만 값이 다름
                    // val1의 로그 id를 val2로 변경
                    existingAlarm.setLog(beverageLogRepository.findById(properLocationLogId).get());
                    alarmRepository.save(existingAlarm);
                }
            }
            // 한쪽 리스트에만 남은 원소가 존재
            else {
                // alarmsOnSameDay에만 값이 남아 있을 때, 즉 변경에 의해 알람을 없애야 하는 경우
                if (existingAlarm != null) {
                    alarmRepository.delete(existingAlarm);
                }

                // appropriatePositions에만 값이 남아 있을 때, 즉 변경에 의해 알람을 추가해야 하는 경우
                if (properLocationLogId != null) {
                    BeverageLog bl = beverageLogRepository.findById(properLocationLogId).get();
                    String content = AlarmMessage.of(properAlarmList.get(i).getRight(),gender);

                    Alarm alarm = Alarm.of(bl, content);
                    alarmRepository.save(alarm);
                }
            }
        }
    }

    // 해당 음료 기록에 해당하는 날짜의 모든 음료 기록들을 가져온다.
    public List<BeverageLog> getLogsOnSameDay(Long userId, BeverageLog beverageLog) {
        LocalDate updatedDate = beverageLog.getUpdatedAt().toLocalDate();
        LocalDateTime startOfDay = updatedDate.atStartOfDay();
        LocalDateTime endOfDay = updatedDate.plusDays(1).atStartOfDay().minusNanos(1);
        return beverageLogRepository.findAllByUserUserIdAndStatusAndUpdatedAtBetween(userId, Status.ACTIVE, startOfDay,endOfDay);
    }

    // 해당 음료 기록에 해당하는 날짜의 모든 알람 기록을 가져온다.
    public List<Alarm> getAlarmsOnSameDay(Long userId, BeverageLog beverageLog) {
        LocalDate updatedDate = beverageLog.getUpdatedAt().toLocalDate();
        LocalDateTime startOfDay = updatedDate.atStartOfDay();
        LocalDateTime endOfDay = updatedDate.plusDays(1).atStartOfDay().minusNanos(1);
        return alarmRepository.findAllByLogUserUserIdAndUpdatedAtBetween(userId,startOfDay,endOfDay);
    }

    // 주어진 음료 섭취 기록들에 대해 적절한 알람 위치를 계산한다.
    private List<Pair<Long, SugarWarning>> getProperAlarmLocation(Gender gender, List<BeverageLog> logsOnSameDay, double cautionAmountOfSugar, double exceedAmountOfSugar) {

        List<Pair<Long, SugarWarning>> properAlarm = new ArrayList<>();

        double accumulatedSugar = 0.0D;
        double creteria = cautionAmountOfSugar;
        for (BeverageLog bl : logsOnSameDay) {
            // 해당 날에 섭취한 당 함량
            accumulatedSugar += bl.getBeverageSize().getSugar() + bl.getAdditionalSugar();
            System.out.println("accumulatedSugar = " + accumulatedSugar);
            // 한번에 초과했을 경우(주의 알람이 필요 없음) 또는 주의 알람 후 당 함량을 초과했을 경우
            if(accumulatedSugar >= exceedAmountOfSugar){
                System.out.println("accumulatedSugar exceeded");
                properAlarm.add(Pair.of(bl.getLogId(),SugarWarning.EXCEED));
                break;
            } else if (accumulatedSugar >= creteria){
                System.out.println("accumulatedSugar is over creteria");
                properAlarm.add(Pair.of(bl.getLogId(),SugarWarning.CAUTION));
                creteria = exceedAmountOfSugar;
            }
        }
        return properAlarm;
    }

}
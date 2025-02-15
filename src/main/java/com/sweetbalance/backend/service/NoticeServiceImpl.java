package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.notice.EachEntry;
import com.sweetbalance.backend.dto.response.notice.ListNoticeDTO;
import com.sweetbalance.backend.entity.Alarm;
import com.sweetbalance.backend.entity.BaseEntity;
import com.sweetbalance.backend.entity.Beverage;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.enums.common.Status;
import com.sweetbalance.backend.repository.AlarmRepository;
import com.sweetbalance.backend.repository.BeverageLogRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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


}

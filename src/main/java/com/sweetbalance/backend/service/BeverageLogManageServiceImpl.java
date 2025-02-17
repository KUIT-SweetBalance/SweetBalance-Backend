package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.entity.*;
import com.sweetbalance.backend.enums.common.Status;
import com.sweetbalance.backend.repository.AlarmRepository;
import com.sweetbalance.backend.repository.BeverageLogRepository;
import com.sweetbalance.backend.repository.BeverageRepository;
import com.sweetbalance.backend.repository.BeverageSizeRepository;
import com.sweetbalance.backend.util.syrup.SugarCalculator;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BeverageLogManageServiceImpl implements BeverageLogManageService {

    private final BeverageRepository beverageRepository;
    private final BeverageSizeRepository beverageSizeRepository;
    private final BeverageLogRepository beverageLogRepository;
    private final AlarmRepository alarmRepository;
    private final SugarCalculator sugarCalculator;
    private final NoticeService noticeService;

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

        noticeService.updateAlarm(user, beverageLog);
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
        noticeService.updateAlarm(user, beverageLog);
    }

    @Override
    public void deleteBeverageRecord(BeverageLog beverageLog) {

        beverageLog.markDeleted();
        beverageLogRepository.save(beverageLog);

        Beverage beverage = beverageLog.getBeverageSize().getBeverage();
        subConsumeCount(beverage);

        User user = beverageLog.getUser();
        noticeService.updateAlarm(user, beverageLog);
    }

    @Override
    public Optional<BeverageLog> findBeverageLogByBeverageLogId(Long beverageLogId) {
        return beverageLogRepository.findById(beverageLogId);
    }

    private void addConsumeCount(Beverage beverage){
        beverage.setConsumeCount(beverage.getConsumeCount() + 1);
        beverageRepository.save(beverage);
    }

    private void subConsumeCount(Beverage beverage){
        beverage.setConsumeCount(beverage.getConsumeCount() - 1);
        beverageRepository.save(beverage);
    }

}

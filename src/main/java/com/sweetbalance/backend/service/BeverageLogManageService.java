package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.request.AddBeverageRecordRequestDTO;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.entity.BeverageSize;
import com.sweetbalance.backend.entity.User;

import java.util.Optional;

public interface BeverageLogManageService {

    // beverage log
    void addBeverageRecord(User user, BeverageSize beverageSize, AddBeverageRecordRequestDTO addBeverageRecordRequestDTO);
    void editBeverageRecord(Long beverageLogId, BeverageSize beverageSize, AddBeverageRecordRequestDTO dto);
    void deleteBeverageRecord(BeverageLog beverageLog);

    Optional<BeverageLog> findBeverageLogByBeverageLogId(Long beverageLogId);
}

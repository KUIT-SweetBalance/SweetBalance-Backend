package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.notice.ListNoticeDTO;
import com.sweetbalance.backend.entity.BeverageLog;
import com.sweetbalance.backend.entity.User;

import java.util.List;

public interface NoticeService {
    List<ListNoticeDTO> getNoticeListByUserId(Long userId);
    void checkNoticeReaded(Long beverageLogId);
    void updateAlarm(User user, BeverageLog beverageLog);
}

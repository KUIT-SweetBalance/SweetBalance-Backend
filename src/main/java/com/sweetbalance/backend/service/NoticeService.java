package com.sweetbalance.backend.service;

import com.sweetbalance.backend.dto.response.notice.ListNoticeDTO;

import java.util.List;

public interface NoticeService {
    List<ListNoticeDTO> getNoticeListByUserId(Long userId);
    void checkNoticeReaded(Long beverageLogId);
}

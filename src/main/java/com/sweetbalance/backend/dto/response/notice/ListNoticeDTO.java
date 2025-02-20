package com.sweetbalance.backend.dto.response.notice;

import java.util.List;

public record ListNoticeDTO (
        String date,               // 예: "2025.02.01"
        List<EachNotice> info   // 해당 날짜의 여러 건 (timeString, message, beverageLogInfo)
) {}


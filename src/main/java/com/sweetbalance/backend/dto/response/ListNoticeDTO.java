package com.sweetbalance.backend.dto.response;

import lombok.*;

public record ListNoticeDTO (String timeString, String message, Object beverageLogInfo) {}

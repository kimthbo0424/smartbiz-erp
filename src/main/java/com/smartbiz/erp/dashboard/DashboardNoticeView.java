package com.smartbiz.erp.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DashboardNoticeView {
    private final Long noticeId;
    private final String title;
    private final String pinnedYn;
    private final LocalDateTime createdAt;
}

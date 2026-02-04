package com.smartbiz.erp.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class DashboardActivityView {
    private final LocalDateTime time;
    private final String actor;
    private final String action;
    private final String detail;
}

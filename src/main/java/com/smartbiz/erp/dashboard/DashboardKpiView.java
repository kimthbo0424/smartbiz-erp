package com.smartbiz.erp.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardKpiView {
    private final long monthLaborCost;
    private final long attendanceIssueCount;
    private final int payRatePercent;
    private final long headcount;
}

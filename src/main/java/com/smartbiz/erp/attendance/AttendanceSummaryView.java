package com.smartbiz.erp.attendance;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class AttendanceSummaryView {

    private List<String> recent4DaysLabels;
    private List<Integer> recent4DaysWorkCounts;

    private DayCompareView dayCompare;

    @Getter
    @AllArgsConstructor
    public static class DayCompareView {
        private int yesterdayWorkCount;
        private int todayWorkCount;

        private int yesterdayHeightPercent;
        private int todayHeightPercent;
    }
}

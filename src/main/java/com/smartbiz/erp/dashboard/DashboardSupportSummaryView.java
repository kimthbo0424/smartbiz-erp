package com.smartbiz.erp.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardSupportSummaryView {
    private final long draftCount;
    private final long receivedCount;
    private final long answeredCount;

    public long getPendingCount() {
        return draftCount + receivedCount;
    }
}

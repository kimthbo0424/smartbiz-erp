package com.smartbiz.erp.dashboard;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DashboardMonthlyPayView {
    private final String ym;
    private final long amount;
}

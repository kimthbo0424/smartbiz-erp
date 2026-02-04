package com.smartbiz.erp.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Model model) {

        LocalDate today = LocalDate.now();
        YearMonth ym = YearMonth.from(today);

        DashboardKpiView kpi = dashboardService.getKpi(ym, today);

        List<DashboardMonthlyPayView> trend = dashboardService.getMonthlyPayTrend(ym, 4);

        List<String> recent4MonthsLabels = trend.stream()
                .map(DashboardMonthlyPayView::getYm)
                .collect(Collectors.toList());

        List<Long> recent4MonthsValues = trend.stream()
                .map(DashboardMonthlyPayView::getAmount)
                .collect(Collectors.toList());

        List<DashboardActivityView> activities = dashboardService.getRecentActivities(10);
        
        List<DashboardNoticeView> notices = dashboardService.getDashboardNotices(8);
        
        DashboardSupportSummaryView supportSummary = dashboardService.getSupportSummary();
        java.time.LocalDateTime latestBackupTime = dashboardService.getLatestBackupTime();

        model.addAttribute("pageTitle", "대시보드");
        model.addAttribute("kpi", kpi);
        model.addAttribute("activities", activities);
        model.addAttribute("today", today);
        model.addAttribute("notices", notices);
        model.addAttribute("supportSummary", supportSummary);
        model.addAttribute("latestBackupTime", latestBackupTime);

        model.addAttribute("recent4MonthsLabels", recent4MonthsLabels);
        model.addAttribute("recent4MonthsValues", recent4MonthsValues);

        return "dashboard";
    }
}

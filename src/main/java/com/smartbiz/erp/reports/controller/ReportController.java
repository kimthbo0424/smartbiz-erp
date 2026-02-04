// src/main/java/com/smartbiz/erp/reports/controller/ReportController.java
package com.smartbiz.erp.reports.controller;

import com.smartbiz.erp.reports.dto.ReportQuery;
import com.smartbiz.erp.reports.dto.ReportView;
import com.smartbiz.erp.reports.service.ReportService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @ModelAttribute("rq")
    public ReportQuery reportQuery() {
        return new ReportQuery();
    }

    @GetMapping
    public String page(@ModelAttribute("rq") ReportQuery rq, Model model) {
        ReportView view = reportService.build(rq);

        model.addAttribute("pageTitle", "SmartBiz ERP - 리포트 & 분석");

        // ✅ 템플릿 호환을 위해 둘 다 내려줌
        model.addAttribute("view", view);
        model.addAttribute("report", view);

        return "reports/reports";
    }
}

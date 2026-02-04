package com.smartbiz.erp.employee.history;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class EmployeeHistoryController {

    private final EmployeeHistoryService employeeHistoryService;

    @GetMapping("/hr-history")
    public String historyList(
            @RequestParam(value = "searchField", required = false) String searchField,
            @RequestParam(value = "keyword", required = false) String keyword,
            Model model
    ) {

        // 기본값 설정
        if (searchField == null || searchField.isBlank()) {
            searchField = "empId"; // 기본 검색 대상은 사번
        }

        List<EmployeeHistoryView> histories =
                employeeHistoryService.getHistoryList(searchField, keyword);

        model.addAttribute("histories", histories);
        model.addAttribute("searchField", searchField);
        model.addAttribute("keyword", keyword);

        return "employee/hr-history";
    }
}

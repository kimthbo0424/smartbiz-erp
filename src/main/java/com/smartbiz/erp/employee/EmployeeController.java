package com.smartbiz.erp.employee;

import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.util.UriComponentsBuilder;

import com.smartbiz.erp.dept.Dept;
import com.smartbiz.erp.dept.DeptService;
import com.smartbiz.erp.employee.history.EmployeeHistoryService;
import com.smartbiz.erp.position.Position;
import com.smartbiz.erp.position.PositionService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final DeptService deptService;
    private final PositionService positionService;
    private final EmployeeHistoryService employeeHistoryService;

    @GetMapping("/employees")
    public String list(
            @RequestParam(value = "searchField", required = false, defaultValue = "name") String searchField,
            @RequestParam(value = "keyword", required = false, defaultValue = "") String keyword,
            @RequestParam(value = "page", required = false) Integer page,
            Model model
    ) {
        // 2. 처음 들어올 때도 url에 page=1이 뜨게
        if (page == null) {
            String redirectUrl = UriComponentsBuilder.fromPath("/employees")
                    .queryParam("searchField", searchField)
                    .queryParam("keyword", keyword)
                    .queryParam("page", 1)
                    .build()
                    .encode()
                    .toUriString();
            return "redirect:" + redirectUrl;
        }

        int pageSize = 15;
        int currentPage = Math.max(1, page);

        // 기존 로직 그대로: 전체 조회 후 자바에서 페이징
        var all = employeeService.findAllWithSearch(searchField, keyword);

        int totalCount = all.size();
        int totalPages = (int) Math.ceil(totalCount / (double) pageSize);

        if (totalPages == 0) totalPages = 1;
        if (currentPage > totalPages) currentPage = totalPages;

        int fromIndex = Math.min((currentPage - 1) * pageSize, totalCount);
        int toIndex = Math.min(fromIndex + pageSize, totalCount);

        var pageList = all.subList(fromIndex, toIndex);

        model.addAttribute("employees", pageList);
        model.addAttribute("searchField", searchField);
        model.addAttribute("keyword", keyword);

        // 페이지 UI용
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("hasPrev", currentPage > 1);
        model.addAttribute("hasNext", currentPage < totalPages);

        return "employee/employees";
    }
    
    @GetMapping("/employee/detail/{id}")
    public String detail(@PathVariable("id") Long id, Model model) {

        Employee e = employeeService.findById(id);

        Map<Long, String> deptNameMap = deptService.findAll().stream()
                .collect(Collectors.toMap(Dept::getDeptId, Dept::getName));

        Map<Long, String> rankNameMap = positionService.getRanks().stream()
                .collect(Collectors.toMap(Position::getPositionId, Position::getName));

        Map<Long, String> titleNameMap = positionService.getTitles().stream()
                .collect(Collectors.toMap(Position::getPositionId, Position::getName));

        String deptName = deptNameMap.getOrDefault(e.getDeptId(), "미지정");
        String rankName = rankNameMap.getOrDefault(e.getRankId(), "");
        String titleName = (e.getTitleId() == null) ? "없음" : titleNameMap.getOrDefault(e.getTitleId(), "없음");

        model.addAttribute("employee", e);
        model.addAttribute("deptName", deptName);
        model.addAttribute("rankName", rankName);
        model.addAttribute("titleName", titleName);

        return "employee/employees-detail";
    }

    @GetMapping("/employee/form")
    public String form(@RequestParam(value = "id", required = false) Long id, Model model) {

        Employee employee;

        if (id == null) {
            employee = new Employee();
            employee.setStatus("재직");
        } else {
            employee = employeeService.findById(id);
        }

        model.addAttribute("employee", employee);
        model.addAttribute("departments", deptService.findAll());
        model.addAttribute("ranks", positionService.getRanks());
        model.addAttribute("titles", positionService.getTitles());

        return "employee/employee-form";
    }

    @PostMapping("/employee/save")
    public String save(@ModelAttribute("employee") Employee employee, HttpSession session) {
        Integer sessionAuth = (Integer) session.getAttribute("auth");
        int auth = (sessionAuth == null ? 3 : sessionAuth);
        
        employeeService.saveFromForm(employee, auth);
        return "redirect:/employees";
    }

    @PostMapping("/employee/delete")
    public String delete(@RequestParam("id") Long id) {
        employeeService.delete(id);
        return "redirect:/employees";
    }
}

package com.smartbiz.erp.attendance;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartbiz.erp.dept.Dept;
import com.smartbiz.erp.dept.DeptService;
import com.smartbiz.erp.employee.Employee;
import com.smartbiz.erp.employee.EmployeeService;
import com.smartbiz.erp.position.PositionService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
public class AttendanceController {

    private final AttendanceService attendanceService;
    private final EmployeeService employeeService;
    private final DeptService deptService;
    private final PositionService positionService;

    @GetMapping("/attendance")
    public String list(
            @RequestParam(name = "searchField", required = false, defaultValue = "name") String searchField,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "workDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate workDate,
            @RequestParam(name = "status", required = false) String statusStr,
            @RequestParam(name = "page", required = false) Integer page,
            Model model
    ) {

        int currentPage = (page == null || page < 1) ? 1 : page;

        Attendance.Status status = null;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            status = Attendance.Status.valueOf(statusStr.trim());
        }

        org.springframework.data.domain.Pageable pageable =
                org.springframework.data.domain.PageRequest.of(
                        currentPage - 1,
                        15,
                        org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "workDate", "attendanceId")
                );

        org.springframework.data.domain.Page<AttendanceListView> result =
                attendanceService.findPage(searchField, keyword, workDate, status, pageable);

        model.addAttribute("attendanceList", result.getContent());
        model.addAttribute("page", result);

        model.addAttribute("totalPages", result.getTotalPages());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("hasPrev", result.hasPrevious());
        model.addAttribute("hasNext", result.hasNext());

        model.addAttribute("statuses", Attendance.Status.values());

        model.addAttribute("searchField", searchField);
        model.addAttribute("keyword", keyword == null ? "" : keyword);
        model.addAttribute("workDate", workDate);
        model.addAttribute("selectedStatus", statusStr == null ? "" : statusStr);

        return "attendance/attendance";
    }


    @GetMapping("/attendance/form")
    public String form(@RequestParam(name = "id", required = false) Long attendanceId,
                       Model model) {

        Attendance a;

        if (attendanceId != null) {
            a = attendanceService.findById(attendanceId);
        } else {
            a = new Attendance();
            a.setWorkDate(LocalDate.now());
            a.setStatus(Attendance.Status.NORMAL);
            a.setOvertimeMinutes(0);
        }

        model.addAttribute("attendance", a);
        model.addAttribute("employees", employeeService.findAll());
        model.addAttribute("statuses", Attendance.Status.values());
        model.addAttribute("today", LocalDate.now());

        return "attendance/attendance-form";
    }


    @PostMapping("/attendance/save")
    public String save(@RequestParam(name = "attendanceId", required = false) Long attendanceId,
                       @RequestParam(name = "employeeId") Long employeeId,
                       @RequestParam(name = "workDate") LocalDate workDate,
                       @RequestParam(name = "checkInTime", required = false) String checkInTime,
                       @RequestParam(name = "checkOutTime", required = false) String checkOutTime,
                       @RequestParam(name = "status", required = false) String statusStr,
                       @RequestParam(name = "overtimeMinutes", required = false) Integer overtimeMinutes,
                       @RequestParam(name = "note", required = false) String note) {

        Attendance.Status status = Attendance.Status.NORMAL;
        if (statusStr != null && !statusStr.trim().isEmpty()) {
            status = Attendance.Status.valueOf(statusStr);
        }

        attendanceService.saveOrUpdate(attendanceId, employeeId, workDate, checkInTime, checkOutTime, status, overtimeMinutes, note);

        return "redirect:/attendance";
    }

    @PostMapping("/attendance/delete")
    public String delete(@RequestParam(name = "id") Long id) {
        attendanceService.delete(id);
        return "redirect:/attendance";
    }

    @GetMapping("/attendance-stats")
    public String stats(
            @RequestParam(name = "deptId", required = false) Long deptId,
            @RequestParam(name = "rankId", required = false) Long rankId,
            @RequestParam(name = "yearMonth", required = false) String yearMonth,
            @RequestParam(name = "searchField", required = false, defaultValue = "name") String searchField,
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "page", required = false) Integer page,
            Model model) {
        // 1) yearMonth 파싱 (없으면 이번달)
        LocalDate now = LocalDate.now();
        int y = now.getYear();
        int m = now.getMonthValue();

        if (yearMonth != null && !yearMonth.isBlank()) {
            try {
                java.time.YearMonth ym = java.time.YearMonth.parse(yearMonth.trim());
                y = ym.getYear();
                m = ym.getMonthValue();
            } catch (Exception e) {
                // 잘못된 값이면 기본(이번달) 유지
            }
        }

        java.time.YearMonth ym = java.time.YearMonth.of(y, m);
        LocalDate rangeStart = ym.atDay(1);
        LocalDate rangeEnd = ym.atEndOfMonth();

        // 2) 직원 필터링 (deptId, rankId)
        List<Employee> employees = employeeService.findAll();

        if (deptId != null) {
            employees = employees.stream()
                    .filter(e -> Objects.equals(e.getDeptId(), deptId))
                    .toList();
        }
        if (rankId != null) {
            employees = employees.stream()
                    .filter(e -> Objects.equals(e.getRankId(), rankId))
                    .toList();
        }
        
        String kw = (keyword == null) ? "" : keyword.trim();
        String sf = (searchField == null || searchField.isBlank()) ? "name" : searchField.trim();

        if (!kw.isEmpty()) {
            if ("id".equalsIgnoreCase(sf)) {
                employees = employees.stream()
                        .filter(e -> e.getEmployeeId() != null && String.valueOf(e.getEmployeeId()).contains(kw))
                        .toList();
            } else {
                employees = employees.stream()
                        .filter(e -> e.getName() != null && e.getName().contains(kw))
                        .toList();
            }
        }

        List<Long> targetEmployeeIds = employees.stream()
                .map(Employee::getEmployeeId)
                .toList();

        // 3) 차트/요약은 기존 로직 유지 (필터된 직원 기준)
        AttendanceSummaryView summary = attendanceService.buildSummaryFor4Days(targetEmployeeIds, now);

        LocalDate yesterday = now.minusDays(1);
        Map<String, Integer> yesterdayMap = attendanceService.buildDayStatusCounts(yesterday, targetEmployeeIds);
        Map<String, Integer> todayMap = attendanceService.buildDayStatusCounts(now, targetEmployeeIds);

        // 4) 직원별 통계 생성 (필터 + 선택월 범위 적용)
        Map<Long, String> empNameMap = employees.stream()
                .collect(Collectors.toMap(Employee::getEmployeeId, Employee::getName));

        Map<Long, String> deptNameMap = deptService.findAll().stream()
                .collect(Collectors.toMap(Dept::getDeptId, Dept::getName));

        List<AttendanceEmployeeStatView> employeeStatsAll = new ArrayList<>();
        for (Employee e : employees) {
            String deptName = deptNameMap.getOrDefault(e.getDeptId(), "");
            AttendanceEmployeeStatView stat = attendanceService.buildEmployeeStat(
                    e.getEmployeeId(),
                    empNameMap.getOrDefault(e.getEmployeeId(), ""),
                    deptName,
                    rangeStart,
                    rangeEnd
            );
            employeeStatsAll.add(stat);
        }

        // 5) 직원별 통계 페이징 (in-memory)
        int pageSize = 5;
        int currentPage = (page == null || page < 1) ? 1 : page;

        int total = employeeStatsAll.size();
        int totalPages = (int) Math.ceil(total / (double) pageSize);
        if (totalPages < 1) totalPages = 1;

        if (currentPage > totalPages) currentPage = totalPages;

        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);

        List<AttendanceEmployeeStatView> employeeStatsPage =
                (fromIndex >= toIndex) ? List.of() : employeeStatsAll.subList(fromIndex, toIndex);

        // 6) 모델 바인딩 (기존 값 유지 + 페이징 값 추가)
        model.addAttribute("departments", deptService.findAll());
        model.addAttribute("ranks", positionService.getRanks());

        model.addAttribute("selectedDeptId", deptId);
        model.addAttribute("selectedRankId", rankId);
        model.addAttribute("selectedYear", y);
        model.addAttribute("selectedMonth", m);

        model.addAttribute("recent4MonthsLabels", summary.getRecent4DaysLabels());
        model.addAttribute("recent4MonthsValues", summary.getRecent4DaysWorkCounts());
        model.addAttribute("dayCompare", summary.getDayCompare());

        model.addAttribute("yesterday", yesterday);
        model.addAttribute("today", now);
        model.addAttribute("yesterdayMap", yesterdayMap);
        model.addAttribute("todayMap", todayMap);

        // 직원별 통계(페이지 적용)
        model.addAttribute("employeeStats", employeeStatsPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("searchField", sf);
        model.addAttribute("keyword", kw);

        return "attendance/attendance-stats";
    }

    
    @PostMapping("/checkout")
    public String checkout(HttpSession session) {

        Long employeeId = (Long) session.getAttribute("employeeId");
        if (employeeId == null) {
            return "redirect:/login";
        }

        try {
            attendanceService.checkout(employeeId);
            return "redirect:/dashboard";
        } catch (IllegalStateException e) {
            // 출근 기록이 없을 때도 일단 대시보드로 보내고 싶다면 이렇게
            return "redirect:/dashboard?checkout=fail";
        }
    }
}

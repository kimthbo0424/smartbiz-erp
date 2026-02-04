package com.smartbiz.erp.attendance;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.smartbiz.erp.employee.Employee;
import com.smartbiz.erp.employee.EmployeeService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;
    private final EmployeeService employeeService;

    public Page<AttendanceListView> findPage(
            String searchField,
            String keyword,
            LocalDate workDate,
            Attendance.Status status,
            Pageable pageable
    ) {
        String sf = (searchField == null || searchField.isBlank()) ? "name" : searchField.trim();
        String kw = (keyword == null) ? "" : keyword.trim();

        return attendanceRepository.searchPage(sf, kw, workDate, status, pageable);
    }

    public Attendance findById(Long attendanceId) {
        return attendanceRepository.findById(attendanceId)
                .orElseThrow(() -> new IllegalArgumentException("attendance not found id=" + attendanceId));
    }

    @Transactional
    public void createTodayCheckInIfAbsent(Long employeeId) {
        LocalDate today = LocalDate.now();

        boolean exists = attendanceRepository.existsByEmployeeIdAndWorkDate(employeeId, today);
        if (exists) {
            return;
        }

        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        Attendance.Status status =
                now.isAfter(LocalTime.of(9, 0))
                        ? Attendance.Status.LATE
                        : Attendance.Status.NORMAL;

        Attendance a = new Attendance();
        a.setEmployeeId(employeeId);
        a.setWorkDate(today);
        a.setCheckInTime(now);
        a.setStatus(status);
        a.setOvertimeMinutes(0);

        attendanceRepository.save(a);
    }

    @Transactional
    public void saveOrUpdate(Long attendanceId,
                             Long employeeId,
                             LocalDate workDate,
                             String checkInTime,
                             String checkOutTime,
                             Attendance.Status status,
                             Integer overtimeMinutes,
                             String note) {

        Attendance target = null;

        if (attendanceId != null) {
            target = attendanceRepository.findById(attendanceId).orElse(null);
        }

        if (target == null) {
            target = attendanceRepository.findByEmployeeIdAndWorkDate(employeeId, workDate).orElse(null);
        }

        if (target == null) {
            target = new Attendance();
            target.setEmployeeId(employeeId);
            target.setWorkDate(workDate);
        }

        target.setStatus(status == null ? Attendance.Status.NORMAL : status);
        target.setOvertimeMinutes(overtimeMinutes == null ? 0 : overtimeMinutes);
        target.setNote(note);

        target.setCheckInTime(parseTimeOrNull(checkInTime));
        target.setCheckOutTime(parseTimeOrNull(checkOutTime));

        attendanceRepository.save(target);
    }

    @Transactional
    public void delete(Long attendanceId) {
        attendanceRepository.deleteById(attendanceId);
    }

    // 최근 4일 차트: 출근(NORMAL) 제외한 모든 상태 합산
    public AttendanceSummaryView buildSummaryFor4Days(List<Long> employeeIds, LocalDate endDayInclusive) {

        List<String> labels = new ArrayList<>();
        List<Integer> counts = new ArrayList<>();

        for (int i = 3; i >= 0; i--) {
            LocalDate d = endDayInclusive.minusDays(i);
            labels.add(d.toString());

            int c = countNotNormal(d, employeeIds);
            counts.add(c);
        }

        LocalDate yesterday = endDayInclusive.minusDays(1);
        int yesterdayAbnormal = countNotNormal(yesterday, employeeIds);
        int todayAbnormal = countNotNormal(endDayInclusive, employeeIds);

        int max = Math.max(yesterdayAbnormal, todayAbnormal);
        int yPct = max == 0 ? 0 : (int) Math.round((yesterdayAbnormal * 100.0) / max);
        int tPct = max == 0 ? 0 : (int) Math.round((todayAbnormal * 100.0) / max);

        AttendanceSummaryView.DayCompareView compare =
                new AttendanceSummaryView.DayCompareView(yesterdayAbnormal, todayAbnormal, yPct, tPct);

        return new AttendanceSummaryView(labels, counts, compare);
    }

    public AttendanceEmployeeStatView buildEmployeeStat(Long employeeId,
                                                        String name,
                                                        String deptName,
                                                        LocalDate start,
                                                        LocalDate end) {

        List<Attendance> rows = attendanceRepository.findAllByEmployeeIdInAndWorkDateBetween(
                List.of(employeeId), start, end
        );

        int normal = 0;
        int late = 0;
        int absent = 0;
        int annual = 0;
        int overtime = 0;

        for (Attendance a : rows) {
            if (a.getStatus() == Attendance.Status.NORMAL) normal++;
            if (a.getStatus() == Attendance.Status.LATE) late++;
            if (a.getStatus() == Attendance.Status.ABSENT) absent++;
            if (a.getStatus() == Attendance.Status.ANNUAL) annual++;
            overtime += a.getOvertimeMinutes() == null ? 0 : a.getOvertimeMinutes();
        }

        return new AttendanceEmployeeStatView(employeeId, name, deptName, normal, late, absent, annual, overtime);
    }

    public Map<String, Integer> buildDayStatusCounts(LocalDate day, List<Long> employeeIds) {

        List<Attendance> list = attendanceRepository.findAllByEmployeeIdInAndWorkDate(employeeIds, day);

        Map<String, Integer> map = new LinkedHashMap<>();
        for (Attendance.Status s : Attendance.Status.values()) {
            map.put(s.name(), 0);
        }

        for (Attendance a : list) {
            String key = a.getStatus().name();
            map.put(key, map.getOrDefault(key, 0) + 1);
        }

        return map;
    }

    private int countNotNormal(LocalDate day, List<Long> employeeIds) {
        List<Attendance> list = attendanceRepository.findAllByEmployeeIdInAndWorkDate(employeeIds, day);

        int cnt = 0;
        for (Attendance a : list) {
            if (a.getStatus() != Attendance.Status.NORMAL) {
                cnt++;
            }
        }
        return cnt;
    }

    private LocalTime parseTimeOrNull(String value) {
        if (value == null) return null;
        String v = value.trim();
        if (v.isEmpty()) return null;
        return LocalTime.parse(v);
    }
    
    @Transactional
    public void checkout(Long employeeId) {
        LocalDate today = LocalDate.now();

        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndWorkDate(employeeId, today)
                .orElseThrow(() -> new IllegalStateException("오늘 출근 기록이 없습니다."));

        // 초, 나노 제거 (09:00:00 같은 형식으로 저장되게)
        LocalTime now = LocalTime.now().withSecond(0).withNano(0);

        attendance.setCheckOutTime(now);
        attendanceRepository.save(attendance);
    }
}

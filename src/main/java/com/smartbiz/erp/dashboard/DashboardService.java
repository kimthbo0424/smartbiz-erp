package com.smartbiz.erp.dashboard;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final JdbcTemplate jdbcTemplate;

    public DashboardKpiView getKpi(YearMonth ym, LocalDate today) {

        long monthLaborCost = querySumNetPayByMonthCalculatedOrConfirmed(ym);

        long attendanceIssueToday = queryLong(
                "SELECT COUNT(*) " +
                        "FROM employee e " +
                        "LEFT JOIN hr_attendance a " +
                        "  ON a.employee_id = e.employee_id " +
                        " AND a.work_date = ? " +
                        "WHERE e.status = '재직' " +
                        "  AND (a.attendance_id IS NULL OR a.status <> 'NORMAL')",
                Date.valueOf(today)
        );

        long payrollTotal = queryLong(
                "SELECT COUNT(*) " +
                        "FROM payroll " +
                        "WHERE year = ? AND month = ?",
                ym.getYear(), ym.getMonthValue()
        );

        long payrollConfirmed = queryLong(
                "SELECT COUNT(*) " +
                        "FROM payroll " +
                        "WHERE year = ? AND month = ? AND status = 'CONFIRMED'",
                ym.getYear(), ym.getMonthValue()
        );

        int payRatePercent = 0;
        if (payrollTotal > 0) {
            payRatePercent = (int) Math.round((payrollConfirmed * 100.0) / payrollTotal);
        }

        long headcount = Math.max(0, payrollTotal - payrollConfirmed);

        return new DashboardKpiView(
                monthLaborCost,
                attendanceIssueToday,
                payRatePercent,
                headcount
        );
    }

    public long querySumNetPayByMonthCalculatedOrConfirmed(YearMonth ym) {
        BigDecimal v = jdbcTemplate.queryForObject(
                "SELECT COALESCE(SUM(net_pay), 0) " +
                        "FROM payroll " +
                        "WHERE year = ? AND month = ? AND status IN ('CALCULATED','CONFIRMED')",
                BigDecimal.class,
                ym.getYear(), ym.getMonthValue()
        );
        if (v == null) return 0L;
        return v.longValue();
    }

    public List<DashboardMonthlyPayView> getMonthlyPayTrend(YearMonth endYm, int months) {

        if (months <= 0) {
            months = 4;
        }

        YearMonth startYm = endYm.minusMonths(months - 1);

        int startKey = startYm.getYear() * 100 + startYm.getMonthValue();
        int endKey = endYm.getYear() * 100 + endYm.getMonthValue();

        String sql =
                "SELECT year, month, COALESCE(SUM(net_pay), 0) AS amt " +
                        "FROM payroll " +
                        "WHERE status IN ('CALCULATED','CONFIRMED') " +
                        "  AND (year * 100 + month) BETWEEN ? AND ? " +
                        "GROUP BY year, month";

        Map<Integer, Long> amtMap = new HashMap<>();

        jdbcTemplate.query(sql, rs -> {
            int y = rs.getInt("year");
            int m = rs.getInt("month");
            BigDecimal bd = rs.getBigDecimal("amt");
            long amt = bd == null ? 0L : bd.longValue();
            amtMap.put(y * 100 + m, amt);
        }, startKey, endKey);

        List<DashboardMonthlyPayView> result = new ArrayList<>();
        for (int i = 0; i < months; i++) {
            YearMonth ym = startYm.plusMonths(i);
            int key = ym.getYear() * 100 + ym.getMonthValue();
            long amount = amtMap.getOrDefault(key, 0L);

            result.add(new DashboardMonthlyPayView(
                    ym.getYear() + "-" + two(ym.getMonthValue()),
                    amount
            ));
        }

        return result;
    }

    public List<DashboardActivityView> getRecentActivities(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 10);

        List<DashboardActivityView> all = new ArrayList<>();
        all.addAll(queryPayrollActivities(safeLimit));
        all.addAll(queryAttendanceActivities(safeLimit));
        all.addAll(queryEmployeeHistoryActivities(safeLimit));

        all.sort(Comparator.comparing(DashboardActivityView::getTime).reversed());

        if (all.size() > safeLimit) {
            return all.subList(0, safeLimit);
        }
        return all;
    }

    private List<DashboardActivityView> queryPayrollActivities(int limit) {
        String sql =
                "SELECT updated_at, employee_id, year, month, status, net_pay " +
                        "FROM payroll " +
                        "ORDER BY updated_at DESC " +
                        "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LocalDateTime t = rs.getTimestamp("updated_at").toLocalDateTime();
            long empId = rs.getLong("employee_id");
            int y = rs.getInt("year");
            int m = rs.getInt("month");
            String status = rs.getString("status");

            BigDecimal bd = rs.getBigDecimal("net_pay");
            long net = bd == null ? 0L : bd.longValue();

            String action = "급여";
            String detail = y + "-" + two(m) + " " + status + " net_pay " + net;

            return new DashboardActivityView(t, String.valueOf(empId), action, detail);
        }, limit);
    }

    private List<DashboardActivityView> queryAttendanceActivities(int limit) {
        String sql =
                "SELECT updated_at, employee_id, work_date, status, overtime_minutes " +
                        "FROM hr_attendance " +
                        "ORDER BY updated_at DESC " +
                        "LIMIT ?";

        return jdbcTemplate.query(sql, (rs, rowNum) -> {
            LocalDateTime t = rs.getTimestamp("updated_at").toLocalDateTime();
            long empId = rs.getLong("employee_id");
            LocalDate d = rs.getDate("work_date").toLocalDate();
            String status = rs.getString("status");
            int ot = rs.getInt("overtime_minutes");

            String action = "근태";
            String detail = d + " " + status + " overtime " + ot;

            return new DashboardActivityView(t, String.valueOf(empId), action, detail);
        }, limit);
    }

    private List<DashboardActivityView> queryEmployeeHistoryActivities(int limit) {

        String sql =
                "SELECT created_at, employee_id, change_type, change_date, note " +
                        "FROM employee_history " +
                        "ORDER BY created_at DESC " +
                        "LIMIT ?";

        try {
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                Timestamp ts = rs.getTimestamp("created_at");
                LocalDateTime t = ts == null ? LocalDateTime.now() : ts.toLocalDateTime();

                long empId = rs.getLong("employee_id");
                String type = rs.getString("change_type");
                LocalDate d = rs.getDate("change_date").toLocalDate();
                String note = rs.getString("note");

                String action = "인사이력";
                String detail = d + " " + type + (note == null ? "" : " " + note);

                return new DashboardActivityView(t, String.valueOf(empId), action, detail);
            }, limit);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private long queryLong(String sql, Object... args) {
        Long v = jdbcTemplate.queryForObject(sql, Long.class, args);
        return v == null ? 0L : v;
    }

    private String two(int n) {
        return n < 10 ? "0" + n : String.valueOf(n);
    }
    
    public List<DashboardNoticeView> getDashboardNotices(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 20);

        List<DashboardNoticeView> result = new ArrayList<>();

        // 1) 고정 + 게시 공지 먼저
        String sqlPinned =
                "SELECT notice_id, title, pinned_yn, created_at " +
                "FROM notice " +
                "WHERE status = 'PUBLISHED' AND pinned_yn = 'Y' " +
                "ORDER BY created_at DESC, notice_id DESC " +
                "LIMIT ?";

        List<DashboardNoticeView> pinned = jdbcTemplate.query(sqlPinned, (rs, rowNum) -> {
            Long id = rs.getLong("notice_id");
            String title = rs.getString("title");
            String pinnedYn = rs.getString("pinned_yn");
            LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
            return new DashboardNoticeView(id, title, pinnedYn, createdAt);
        }, safeLimit);

        result.addAll(pinned);

        int remaining = safeLimit - result.size();
        if (remaining <= 0) {
            return result;
        }

        // 2) 남은 개수만큼 비고정 + 게시 공지 최신순
        String sqlLatest =
                "SELECT notice_id, title, pinned_yn, created_at " +
                "FROM notice " +
                "WHERE status = 'PUBLISHED' AND pinned_yn <> 'Y' " +
                "ORDER BY created_at DESC, notice_id DESC " +
                "LIMIT ?";

        List<DashboardNoticeView> latest = jdbcTemplate.query(sqlLatest, (rs, rowNum) -> {
            Long id = rs.getLong("notice_id");
            String title = rs.getString("title");
            String pinnedYn = rs.getString("pinned_yn");
            LocalDateTime createdAt = rs.getTimestamp("created_at").toLocalDateTime();
            return new DashboardNoticeView(id, title, pinnedYn, createdAt);
        }, remaining);

        result.addAll(latest);

        return result;
    }

    public DashboardSupportSummaryView getSupportSummary() {

        long draft = queryLong(
                "SELECT COUNT(*) FROM support WHERE status = 'DRAFT'"
        );

        long received = queryLong(
                "SELECT COUNT(*) FROM support WHERE status = 'RECEIVED'"
        );

        long answered = queryLong(
                "SELECT COUNT(*) FROM support WHERE status = 'ANSWERED'"
        );

        return new DashboardSupportSummaryView(draft, received, answered);
    }

    public LocalDateTime getLatestBackupTime() {

        try {
            Timestamp ts = jdbcTemplate.queryForObject(
                    "SELECT MAX(backup_time) FROM ops_backup",
                    Timestamp.class
            );
            return ts == null ? null : ts.toLocalDateTime();
        } catch (Exception e) {
            return null;
        }
    }

}

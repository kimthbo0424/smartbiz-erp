package com.smartbiz.erp.attendance;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendanceListView {

    private Long attendanceId;

    private Long employeeId;
    private String employeeName;

    private LocalDate workDate;

    private LocalTime checkInTime;
    private LocalTime checkOutTime;

    private String status;

    private Integer overtimeMinutes;

    private String note;

    public AttendanceListView(
            Long attendanceId,
            Long employeeId,
            String employeeName,
            LocalDate workDate,
            LocalTime checkInTime,
            LocalTime checkOutTime,
            Attendance.Status status,
            Integer overtimeMinutes,
            String note
    ) {
        this(
                attendanceId,
                employeeId,
                employeeName,
                workDate,
                checkInTime,
                checkOutTime,
                status == null ? null : status.name(),
                overtimeMinutes,
                note
        );
    }
}

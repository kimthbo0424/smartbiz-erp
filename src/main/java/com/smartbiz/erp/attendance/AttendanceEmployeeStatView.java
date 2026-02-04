package com.smartbiz.erp.attendance;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AttendanceEmployeeStatView {

    private Long employeeId;
    private String name;
    private String deptName;

    private int normalCount;
    private int lateCount;
    private int absentCount;
    private int annualCount;

    private int overtimeMinutes;
}

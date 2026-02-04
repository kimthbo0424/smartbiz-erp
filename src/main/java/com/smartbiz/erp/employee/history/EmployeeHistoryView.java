package com.smartbiz.erp.employee.history;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeHistoryView {

    private Long empId;
    private String empName;
    private LocalDate changeDate;
    private String beforeDeptName;
    private String afterDeptName;
    private String beforePositionName;
    private String afterPositionName;
    private String changeType;
}

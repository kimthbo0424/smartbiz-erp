package com.smartbiz.erp.employee;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
public class EmployeeListView {

    private Long employeeId;
    private String name;
    private String deptName;
    private String rankName;
    private String titleName;
    private LocalDate hireDate;
    private String status;
}

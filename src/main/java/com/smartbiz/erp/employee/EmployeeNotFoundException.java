package com.smartbiz.erp.employee;

public class EmployeeNotFoundException extends RuntimeException {

    public EmployeeNotFoundException(String str) {
        super(str);
    }
}

// src/main/java/com/smartbiz/erp/reports/dto/ReportQuery.java
package com.smartbiz.erp.reports.dto;

public class ReportQuery {
    // yyyy-MM (ex: 2025-11)
    private String fromMonth;
    private String toMonth;
    private String dept; // 1차는 UI만 유지 (미사용)

    public String getFromMonth() { return fromMonth; }
    public void setFromMonth(String fromMonth) { this.fromMonth = fromMonth; }

    public String getToMonth() { return toMonth; }
    public void setToMonth(String toMonth) { this.toMonth = toMonth; }

    public String getDept() { return dept; }
    public void setDept(String dept) { this.dept = dept; }
}

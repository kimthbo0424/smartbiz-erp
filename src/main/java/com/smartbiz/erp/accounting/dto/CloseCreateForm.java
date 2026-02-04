package com.smartbiz.erp.accounting.dto;

public class CloseCreateForm {

    /**
     * MONTH / QUARTER / YEAR
     */
    private String type;

    /**
     * MONTH: yyyy-MM, QUARTER: yyyy-Qn, YEAR: yyyy
     */
    private String period;

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public String getPeriod() { return period; }
    public void setPeriod(String period) { this.period = period; }
}

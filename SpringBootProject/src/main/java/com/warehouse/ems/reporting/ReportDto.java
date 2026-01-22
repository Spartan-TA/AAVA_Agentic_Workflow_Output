package com.warehouse.ems.reporting;

import java.time.LocalDate;

/**
 * DTO for reporting and analytics.
 */
public class ReportDto {
    private String type;
    private String department;
    private String shift;
    private LocalDate date;
    private String employeeName;
    private String value;

    // Getters and setters
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getShift() { return shift; }
    public void setShift(String shift) { this.shift = shift; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    public String getValue() { return value; }
    public void setValue(String value) { this.value = value; }
}

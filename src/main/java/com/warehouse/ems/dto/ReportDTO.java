package com.warehouse.ems.dto;

import java.time.LocalDateTime;

public class ReportDTO {
    private String reportType;
    private Object data;
    private LocalDateTime generatedDate;
    private String format;

    // Getters and Setters
    public String getReportType() { return reportType; }
    public void setReportType(String reportType) { this.reportType = reportType; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    public LocalDateTime getGeneratedDate() { return generatedDate; }
    public void setGeneratedDate(LocalDateTime generatedDate) { this.generatedDate = generatedDate; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}

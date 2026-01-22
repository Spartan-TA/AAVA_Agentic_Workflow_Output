package com.warehouse.ems.payroll;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for Payroll export with validation annotations.
 */
public class PayrollExportDto {
    @NotNull(message = "Start date is required.")
    private LocalDate startDate;

    @NotNull(message = "End date is required.")
    private LocalDate endDate;

    @NotNull(message = "Format is required.")
    private String format;

    // Getters and setters
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public String getFormat() { return format; }
    public void setFormat(String format) { this.format = format; }
}

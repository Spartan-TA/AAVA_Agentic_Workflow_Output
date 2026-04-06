package com.warehouse.ems.dto;

import java.time.LocalDate;

public class AssetCheckoutDTO {
    private Long employeeId;
    private LocalDate checkoutDate;
    private LocalDate expectedReturnDate;

    // Getters and Setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDate getCheckoutDate() { return checkoutDate; }
    public void setCheckoutDate(LocalDate checkoutDate) { this.checkoutDate = checkoutDate; }
    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }
}

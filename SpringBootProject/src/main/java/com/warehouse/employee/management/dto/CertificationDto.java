package com.warehouse.employee.management.dto;

import javax.validation.constraints.*;
import java.time.LocalDate;

public class CertificationDto {
    @NotBlank
    private String name;

    @NotNull
    private LocalDate expiryDate;

    @NotNull
    private Long employeeId;

    @NotBlank
    private String documentUrl;

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
}

package com.warehouse.ems.certification;

import javax.validation.constraints.Future;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO for Certification with validation annotations.
 */
public class CertificationDto {
    private Long id;

    @NotNull(message = "Employee ID is required.")
    private Long employeeId;

    @NotNull(message = "Certification type is required.")
    @Size(min = 2, max = 128, message = "Type must be between 2 and 128 characters.")
    private String type;

    @NotNull(message = "Expiry date is required.")
    @Future(message = "Expiry date must be in the future.")
    private LocalDate expiryDate;

    @Size(max = 512, message = "Document URL too long.")
    private String documentUrl;

    @NotNull(message = "Status is required.")
    private String status;

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}

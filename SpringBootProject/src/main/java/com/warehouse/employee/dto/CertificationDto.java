package com.warehouse.employee.dto;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * DTO for employee certification.
 */
public class CertificationDto {
    private Long certificationId;
    @NotNull
    private Long employeeId;
    @NotNull
    private String certificationName;
    @NotNull
    private LocalDate issueDate;
    private LocalDate expiryDate;

    public CertificationDto() {}

    public CertificationDto(Long certificationId, Long employeeId, String certificationName, LocalDate issueDate, LocalDate expiryDate) {
        this.certificationId = certificationId;
        this.employeeId = employeeId;
        this.certificationName = certificationName;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
    }

    public Long getCertificationId() {
        return certificationId;
    }

    public void setCertificationId(Long certificationId) {
        this.certificationId = certificationId;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(Long employeeId) {
        this.employeeId = employeeId;
    }

    public String getCertificationName() {
        return certificationName;
    }

    public void setCertificationName(String certificationName) {
        this.certificationName = certificationName;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }
}

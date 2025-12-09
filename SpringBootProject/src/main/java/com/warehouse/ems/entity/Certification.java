package com.warehouse.ems.entity;

import javax.persistence.*;
import javax.validation.constraints.Future;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Entity representing an employee certification with expiration tracking.
 */
@Entity
@Table(name = "certifications")
public class Certification {

    /**
     * Unique identifier for the certification.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the certification.
     */
    @NotBlank(message = "Certification name is required.")
    @Column(nullable = false)
    private String name;

    /**
     * Issuing authority or organization.
     */
    @NotBlank(message = "Issuing authority is required.")
    @Column(nullable = false)
    private String issuedBy;

    /**
     * Date when the certification was issued.
     */
    @NotNull(message = "Issue date is required.")
    @Column(nullable = false)
    private LocalDate issueDate;

    /**
     * Expiration date of the certification.
     */
    @NotNull(message = "Expiration date is required.")
    @Future(message = "Expiration date must be in the future.")
    @Column(nullable = false)
    private LocalDate expirationDate;

    /**
     * Employee who owns this certification.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    // Getters and setters

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIssuedBy() {
        return issuedBy;
    }

    public void setIssuedBy(String issuedBy) {
        this.issuedBy = issuedBy;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public void setIssueDate(LocalDate issueDate) {
        this.issueDate = issueDate;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }
}

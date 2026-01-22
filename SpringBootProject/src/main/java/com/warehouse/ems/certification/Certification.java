package com.warehouse.ems.certification;

import com.warehouse.ems.employee.Employee;
import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

/**
 * Entity representing an employee certification for training & compliance.
 */
@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    @NotNull(message = "Employee is required.")
    private Employee employee;

    @NotNull(message = "Certification type is required.")
    @Column(nullable = false)
    private String type;

    @NotNull(message = "Expiry date is required.")
    @Column(nullable = false)
    private LocalDate expiryDate;

    @Column(length = 512)
    private String documentUrl;

    @Enumerated(EnumType.STRING)
    @NotNull(message = "Certification status is required.")
    @Column(nullable = false)
    private Status status;

    public enum Status {
        ACTIVE,
        EXPIRED,
        PENDING_RENEWAL
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }

    public String getDocumentUrl() { return documentUrl; }
    public void setDocumentUrl(String documentUrl) { this.documentUrl = documentUrl; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
}

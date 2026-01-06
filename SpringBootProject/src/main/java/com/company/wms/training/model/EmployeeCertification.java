package com.company.wms.training.model;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing the assignment of a Certification to an Employee.
 */
@Entity
@Table(name = "employee_certifications")
public class EmployeeCertification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee who holds the certification.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "employee_id", nullable = false)
    private Employee employee;

    /**
     * The certification held by the employee.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "certification_id", nullable = false)
    private Certification certification;

    /**
     * Date the certification was assigned to the employee.
     */
    @Column(name = "assigned_date")
    private LocalDate assignedDate;

    /**
     * Date the certification expires for the employee.
     */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // Constructors, getters, setters, equals, hashCode, toString

    public EmployeeCertification() {}

    public EmployeeCertification(Employee employee, Certification certification, LocalDate assignedDate, LocalDate expiryDate) {
        this.employee = employee;
        this.certification = certification;
        this.assignedDate = assignedDate;
        this.expiryDate = expiryDate;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Employee getEmployee() {
        return employee;
    }

    public void setEmployee(Employee employee) {
        this.employee = employee;
    }

    public Certification getCertification() {
        return certification;
    }

    public void setCertification(Certification certification) {
        this.certification = certification;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public LocalDate getExpiryDate() {
        return expiryDate;
    }

    public void setExpiryDate(LocalDate expiryDate) {
        this.expiryDate = expiryDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        EmployeeCertification that = (EmployeeCertification) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "EmployeeCertification{" +
                "id=" + id +
                ", employee=" + (employee != null ? employee.getId() : null) +
                ", certification=" + (certification != null ? certification.getId() : null) +
                ", assignedDate=" + assignedDate +
                ", expiryDate=" + expiryDate +
                '}';
    }
}

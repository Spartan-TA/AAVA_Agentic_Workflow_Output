package com.company.wms.training.model;

import javax.persistence.*;
import java.time.LocalDate;

/**
 * Entity representing a Certification held by an employee.
 */
@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Name of the certification (e.g., OSHA, Forklift License).
     */
    @Column(nullable = false)
    private String name;

    /**
     * Issuing authority or organization.
     */
    @Column(nullable = false)
    private String authority;

    /**
     * Date the certification was issued.
     */
    @Column(name = "issued_date")
    private LocalDate issuedDate;

    /**
     * Expiry date of the certification, if applicable.
     */
    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    // Constructors, getters, setters, equals, hashCode, toString

    public Certification() {}

    public Certification(String name, String authority, LocalDate issuedDate, LocalDate expiryDate) {
        this.name = name;
        this.authority = authority;
        this.issuedDate = issuedDate;
        this.expiryDate = expiryDate;
    }

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

    public String getAuthority() {
        return authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDate issuedDate) {
        this.issuedDate = issuedDate;
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
        Certification that = (Certification) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Certification{" +
                "id=" + id +
                ", name='" + name + ''' +
                ", authority='" + authority + ''' +
                ", issuedDate=" + issuedDate +
                ", expiryDate=" + expiryDate +
                '}';
    }
}

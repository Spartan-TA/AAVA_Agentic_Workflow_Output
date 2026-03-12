package com.company.wems.domain;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "certifications")
public class Certification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "validity_period_months")
    private Integer validityPeriodMonths;

    // Constructors, getters, setters
    public Certification() {}

    public Certification(String name, String description, Integer validityPeriodMonths) {
        this.name = name;
        this.description = description;
        this.validityPeriodMonths = validityPeriodMonths;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getValidityPeriodMonths() {
        return validityPeriodMonths;
    }

    public void setValidityPeriodMonths(Integer validityPeriodMonths) {
        this.validityPeriodMonths = validityPeriodMonths;
    }
}

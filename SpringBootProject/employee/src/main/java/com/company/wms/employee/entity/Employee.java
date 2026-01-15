package com.company.wms.employee.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Entity representing an Employee in the Warehouse Management System.
 */
@Entity
@Table(name = "employees")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @Email(message = "Email should be valid")
    @NotBlank(message = "Email is required")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Phone number is required")
    @Column(nullable = false)
    private String phoneNumber;

    @PastOrPresent(message = "Hire date cannot be in the future")
    @Column(nullable = false)
    private LocalDate hireDate;

    @NotBlank(message = "Job title is required")
    @Column(nullable = false)
    private String jobTitle;

    @NotNull(message = "Active status is required")
    @Column(nullable = false)
    private Boolean active;

    // Additional fields can be added as needed

    /**
     * Returns the full name of the employee.
     * @return full name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}

package com.warehouse.ems.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.time.LocalDate;

@Entity
@Data
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String firstName;

    @NotBlank
    private String lastName;

    @Email
    @NotBlank
    @Column(unique = true)
    private String email;

    @NotBlank
    private String phone;

    @PastOrPresent
    private LocalDate hireDate;

    @NotBlank
    private String department;

    @NotBlank
    private String position;

    @NotBlank
    private String status;

    @NotNull
    private Long warehouseId;
}
package com.warehouse.ems.domain;

import com.warehouse.ems.enums.EmployeeStatus;
import com.warehouse.ems.enums.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.*;
import java.time.LocalDate;

/**
 * Entity representing a warehouse employee.
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

    @NotBlank
    @Size(max = 100)
    private String name;

    @NotBlank
    @Column(unique = true, nullable = false)
    @Size(max = 20)
    private String badgeId;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @NotBlank
    @Size(max = 50)
    private String department;

    @Size(max = 50)
    private String shiftGroup;

    @PastOrPresent
    private LocalDate hireDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private EmployeeStatus status;
}

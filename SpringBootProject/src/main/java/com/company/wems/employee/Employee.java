package com.company.wems.employee;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(name = "name", nullable = false)
    private String name;

    @NotBlank
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Column(name = "role", nullable = false)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @NotBlank
    @Column(name = "department", nullable = false)
    private String department;

    @Column(name = "shift_group")
    private String shiftGroup;

    @PastOrPresent
    @Column(name = "hire_date")
    private LocalDate hireDate;

    @NotBlank
    @Column(name = "status", nullable = false)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
}
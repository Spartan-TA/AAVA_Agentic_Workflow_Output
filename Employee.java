package com.wms.ems.employee.model;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee", uniqueConstraints = {@UniqueConstraint(columnNames = "badge_id")})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    @NotBlank
    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @NotBlank
    @Column(nullable = false)
    private String role;

    @NotBlank
    @Column(nullable = false)
    private String department;

    @NotBlank
    @Column(name = "shift_group", nullable = false)
    private String shiftGroup;

    @NotNull
    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @NotBlank
    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private boolean deleted = false;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    @LastModifiedDate
    @Column(name = "last_modified_date")
    private LocalDateTime lastModifiedDate;
}

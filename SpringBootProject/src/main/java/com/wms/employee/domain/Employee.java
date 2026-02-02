package com.wms.employee.domain;

import com.wms.common.domain.AuditableEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.Where;

import java.time.LocalDate;

/**
 * Employee entity representing warehouse employees.
 */
@Entity
@Table(name = "employees")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@SQLDelete(sql = "UPDATE employees SET deleted = true WHERE id = ?")
@Where(clause = "deleted = false")
public class Employee extends AuditableEntity {
    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String firstName;

    @NotBlank
    @Size(max = 100)
    @Column(nullable = false)
    private String lastName;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true)
    private String email;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @Column(nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    private boolean deleted = false;
}

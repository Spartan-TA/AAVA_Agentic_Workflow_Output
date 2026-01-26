package com.wms.ems.employee.entity;

import com.wms.ems.department.entity.Department;
import com.wms.ems.shift.entity.ShiftGroup;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "employee")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String role;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "department_id")
    private Department department;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shift_group_id")
    private ShiftGroup shiftGroup;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false)
    private String status;

    @Column(nullable = false)
    private Boolean deleted = false;
}

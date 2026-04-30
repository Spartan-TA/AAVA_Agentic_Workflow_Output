package com.warehouse.ems.entity;

import lombok.*;
import javax.persistence.*;
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

    @Column(name = "badge_id", nullable = false, unique = true, length = 32)
    private String badgeId;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 32)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER

    @Column(length = 64)
    private String department;

    @Column(name = "shift_group", length = 64)
    private String shiftGroup;

    @Column(name = "hire_date", nullable = false)
    private LocalDate hireDate;

    @Column(nullable = false, length = 16)
    private String status; // ACTIVE, INACTIVE, TERMINATED

    @Column(name = "deleted")
    private Boolean deleted = false;

    @Column(name = "warehouse_id", nullable = false)
    private Integer warehouseId;

    @Column(name = "created_at")
    private java.time.LocalDateTime createdAt;

    @Column(name = "updated_at")
    private java.time.LocalDateTime updatedAt;
}
package com.warehouse.ems.domain.entity;

import lombok.Data;
import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Data
public class Employee {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "badge_id", nullable = false, unique = true, length = 64)
    private String badgeId;
    
    @Column(nullable = false, length = 128)
    private String name;
    
    @Column(nullable = false, length = 32)
    private String role; // ADMIN, HR, SUPERVISOR, WORKER
    
    @Column(nullable = false, length = 64)
    private String department;
    
    @Column(name = "shift_group", length = 64)
    private String shiftGroup;
    
    @Column(name = "hire_date")
    private LocalDate hireDate;
    
    @Column(nullable = false, length = 32)
    private String status; // ACTIVE, INACTIVE, TERMINATED
    
    @Column(name = "created_at")
    private LocalDate createdAt;
    
    @Column(name = "updated_at")
    private LocalDate updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDate.now();
        updatedAt = LocalDate.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDate.now();
    }
}
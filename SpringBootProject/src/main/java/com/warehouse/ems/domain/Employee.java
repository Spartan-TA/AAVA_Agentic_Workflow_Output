package com.warehouse.ems.domain;

import lombok.*;
import javax.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Employee JPA entity for warehouse employee records.
 */
@Entity
@Table(name = "employees", uniqueConstraints = @UniqueConstraint(columnNames = "badge_id"))
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {
    @Id
    @GeneratedValue
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "badge_id", nullable = false, unique = true)
    private String badgeId;

    @Column(nullable = false)
    private String role;

    private String department;
    private String shiftGroup;
    private LocalDate hireDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status;

    public enum Status {
        ACTIVE, INACTIVE, DELETED
    }
}

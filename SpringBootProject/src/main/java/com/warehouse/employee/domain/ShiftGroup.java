package com.warehouse.employee.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * ShiftGroup entity for grouping employees by shift.
 */
@Entity
@Table(name = "shift_groups")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShiftGroup {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
}

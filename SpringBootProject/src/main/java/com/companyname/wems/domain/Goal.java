package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

/**
 * Goal entity for tracking employee goals.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "goals")
public class Goal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotBlank
    @Size(max = 255)
    private String description;

    @NotBlank
    @Size(max = 20)
    private String status; // OPEN, COMPLETED, CANCELLED

    private LocalDate dueDate;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}

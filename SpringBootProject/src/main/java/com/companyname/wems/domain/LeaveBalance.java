package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

/**
 * LeaveBalance entity for tracking employee leave balances.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leave_balances")
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotBlank
    @Size(max = 20)
    private String type; // PTO, SICK, UNPAID

    @NotNull
    @Column(nullable = false)
    private Double balance;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}

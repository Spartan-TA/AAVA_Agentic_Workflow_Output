package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import jakarta.validation.constraints.*;

/**
 * LeavePolicy entity for defining leave policies for employees.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leave_policies")
public class LeavePolicy {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 50)
    private String type; // PTO, SICK, UNPAID

    @NotNull
    @Column(nullable = false)
    private Double annualEntitlement;

    @NotNull
    @Column(nullable = false)
    private Double carryForwardLimit;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}

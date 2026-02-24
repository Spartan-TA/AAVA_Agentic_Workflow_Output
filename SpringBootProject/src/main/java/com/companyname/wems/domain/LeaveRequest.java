package com.companyname.wems.domain;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import jakarta.validation.constraints.*;

/**
 * LeaveRequest entity for managing employee leave requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "leave_requests")
public class LeaveRequest {
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
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotBlank
    @Size(max = 20)
    private String status; // PENDING, APPROVED, REJECTED

    @Size(max = 255)
    private String reason;

    @NotNull
    @Column(nullable = false)
    private Long tenantId;
}

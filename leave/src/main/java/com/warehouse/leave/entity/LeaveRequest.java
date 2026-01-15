package com.warehouse.leave.entity;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;

@Entity
@Table(name = "leave_requests")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private LeaveType type;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private LeaveStatus status;

    @NotNull
    private Double balance;

    @NotNull
    private Long employeeId;

    public enum LeaveType {
        SICK, VACATION, PERSONAL, UNPAID, OTHER
    }

    public enum LeaveStatus {
        REQUESTED, APPROVED, DENIED, CANCELLED
    }
}

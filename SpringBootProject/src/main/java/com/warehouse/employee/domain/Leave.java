package com.warehouse.employee.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Leave entity for employee leave requests.
 */
@Entity
@Table(name = "leaves", indexes = {
        @Index(name = "idx_leave_employee", columnList = "employee_id"),
        @Index(name = "idx_leave_status", columnList = "status")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leave {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * The employee requesting leave.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", nullable = false)
    @JsonIgnore
    private Employee employee;

    @NotNull
    @Enumerated(EnumType.STRING)
    private LeaveType leaveType;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Min(0)
    private Double totalDays;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    @Size(max = 500)
    private String reason;

    @Size(max = 100)
    private String approvedBy;

    private LocalDateTime approvedAt;

    @Min(0)
    private Double balance;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    /**
     * Leave types.
     */
    public enum LeaveType {
        PTO, SICK, UNPAID, BEREAVEMENT
    }

    /**
     * Leave status.
     */
    public enum Status {
        PENDING, APPROVED, REJECTED
    }
}

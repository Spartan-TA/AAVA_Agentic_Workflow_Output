package com.companyname.wems.leave.model;

import lombok.*;
import javax.persistence.*;
import javax.validation.constraints.*;

@Entity
@Table(name = "leave_balances")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveBalance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(nullable = false)
    private Long employeeId;

    @NotBlank
    @Column(nullable = false)
    private String leaveType;

    @NotNull
    @Column(nullable = false)
    private Double balance;

    @NotNull
    @Column(nullable = false)
    private Double accrualRate;
}
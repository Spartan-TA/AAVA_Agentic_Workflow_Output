package com.warehouse.management.dto;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaveApprovalDTO {
    @NotNull
    private Long leaveRequestId;

    @NotNull
    private Long approverId;

    @NotBlank
    @Size(max = 50)
    private String status;

    @Size(max = 255)
    private String comments;
}
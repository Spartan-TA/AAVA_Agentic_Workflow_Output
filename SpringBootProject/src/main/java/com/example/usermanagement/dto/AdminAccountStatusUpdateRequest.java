package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AdminAccountStatusUpdateRequest {
    @NotNull
    private Boolean activate;
}

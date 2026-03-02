package com.example.usermanagement.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MFASetupRequest {
    @NotBlank
    private String password;
}

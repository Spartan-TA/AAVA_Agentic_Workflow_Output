package com.example.app.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class ChangePasswordDto {
    @NotBlank
    private String oldPassword;
    @NotBlank
    private String newPassword;
}

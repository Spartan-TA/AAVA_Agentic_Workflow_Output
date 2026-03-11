package com.example.app.dto;

import lombok.Data;
import javax.validation.constraints.NotBlank;

@Data
public class MfaDto {
    @NotBlank
    private String code;
}

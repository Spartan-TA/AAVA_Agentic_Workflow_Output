package com.example.usermanagement.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLoginResponse {
    private String token;
    private boolean mfaRequired;
    private String mfaQrCodeUrl;
}

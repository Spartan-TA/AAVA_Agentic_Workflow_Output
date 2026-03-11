package com.example.app.service;

import com.example.app.dto.LoginDto;
import com.example.app.dto.JwtResponseDto;
import com.example.app.dto.MfaDto;

public interface AuthService {
    JwtResponseDto authenticateUser(LoginDto loginDto);
    void initiateMfa(String username);
    boolean verifyMfa(String username, MfaDto mfaDto);
    void sendVerificationEmail(String email);
    boolean verifyToken(String token);
    void logout(String username);
}

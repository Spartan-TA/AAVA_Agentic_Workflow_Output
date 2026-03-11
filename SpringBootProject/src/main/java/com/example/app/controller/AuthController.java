package com.example.app.controller;

import com.example.app.dto.LoginDto;
import com.example.app.dto.JwtResponseDto;
import com.example.app.dto.MfaDto;
import com.example.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponseDto> login(@RequestBody LoginDto loginDto) {
        JwtResponseDto response = authService.authenticateUser(loginDto);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/mfa/initiate")
    public ResponseEntity<Void> initiateMfa(@RequestParam String username) {
        authService.initiateMfa(username);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/mfa/verify")
    public ResponseEntity<Boolean> verifyMfa(@RequestParam String username, @RequestBody MfaDto mfaDto) {
        boolean result = authService.verifyMfa(username, mfaDto);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/send-verification-email")
    public ResponseEntity<Void> sendVerificationEmail(@RequestParam String email) {
        authService.sendVerificationEmail(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify")
    public ResponseEntity<Boolean> verifyToken(@RequestParam String token) {
        boolean result = authService.verifyToken(token);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestParam String username) {
        authService.logout(username);
        return ResponseEntity.ok().build();
    }
}

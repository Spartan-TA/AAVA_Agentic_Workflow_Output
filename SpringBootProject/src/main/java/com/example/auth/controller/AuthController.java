package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for authentication and user management.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<UserDto> login(@RequestBody LoginRequestDto loginRequestDto) {
        return ResponseEntity.ok(authService.login(loginRequestDto));
    }

    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody RegisterRequestDto registerRequestDto) {
        return ResponseEntity.ok(authService.register(registerRequestDto));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserDto> updateProfile(@RequestParam Long userId, @RequestBody ProfileUpdateDto profileUpdateDto) {
        return ResponseEntity.ok(authService.updateProfile(userId, profileUpdateDto));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserDto> getProfile(@RequestParam Long userId) {
        return ResponseEntity.ok(authService.getUserProfile(userId));
    }

    @PostMapping("/password-reset/initiate")
    public ResponseEntity<Void> initiatePasswordReset(@RequestParam String email) {
        authService.initiatePasswordReset(email);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/password-reset/complete")
    public ResponseEntity<Void> resetPassword(@RequestBody PasswordResetDto passwordResetDto) {
        authService.resetPassword(passwordResetDto);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteUser(@RequestParam Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok().build();
    }
}

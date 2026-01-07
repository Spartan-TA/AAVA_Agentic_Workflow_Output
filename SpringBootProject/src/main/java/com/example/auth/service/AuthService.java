package com.example.auth.service;

import com.example.auth.dto.*;
import com.example.auth.entity.User;

/**
 * Service interface for authentication and user management.
 */
public interface AuthService {
    UserDto login(LoginRequestDto loginRequestDto);
    UserDto register(RegisterRequestDto registerRequestDto);
    UserDto updateProfile(Long userId, ProfileUpdateDto profileUpdateDto);
    void initiatePasswordReset(String email);
    void resetPassword(PasswordResetDto passwordResetDto);
    void deleteUser(Long userId);
    UserDto getUserProfile(Long userId);
}

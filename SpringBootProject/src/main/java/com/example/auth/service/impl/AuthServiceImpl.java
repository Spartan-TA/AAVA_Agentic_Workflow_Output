package com.example.auth.service.impl;

import com.example.auth.dto.*;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * Implementation of AuthService.
 */
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.lockout.maxAttempts:5}")
    private int maxAttempts;
    @Value("${app.lockout.duration:15}")
    private int lockoutDurationMinutes;

    @Override
    public UserDto login(LoginRequestDto loginRequestDto) {
        Optional<User> userOpt = userRepository.findByEmail(loginRequestDto.getEmail());
        if (userOpt.isEmpty()) {
            throw new RuntimeException("Invalid credentials");
        }
        User user = userOpt.get();
        if (user.isLocked()) {
            if (user.getLockoutTime() != null && user.getLockoutTime().plusMinutes(lockoutDurationMinutes).isBefore(LocalDateTime.now())) {
                user.setLocked(false);
                user.setFailedLoginAttempts(0);
                user.setLockoutTime(null);
            } else {
                throw new RuntimeException("Account is locked. Try again later.");
            }
        }
        if (!passwordEncoder.matches(loginRequestDto.getPassword(), user.getPassword())) {
            user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
            if (user.getFailedLoginAttempts() >= maxAttempts) {
                user.setLocked(true);
                user.setLockoutTime(LocalDateTime.now());
            }
            userRepository.save(user);
            throw new RuntimeException("Invalid credentials");
        }
        user.setFailedLoginAttempts(0);
        userRepository.save(user);
        return toDto(user);
    }

    @Override
    public UserDto register(RegisterRequestDto registerRequestDto) {
        if (userRepository.existsByEmail(registerRequestDto.getEmail())) {
            throw new RuntimeException("Email already registered");
        }
        User user = User.builder()
                .email(registerRequestDto.getEmail())
                .password(passwordEncoder.encode(registerRequestDto.getPassword()))
                .fullName(registerRequestDto.getFullName())
                .emailVerified(false)
                .locked(false)
                .failedLoginAttempts(0)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .deleted(false)
                .build();
        userRepository.save(user);
        // TODO: Send verification email
        return toDto(user);
    }

    @Override
    public UserDto updateProfile(Long userId, ProfileUpdateDto profileUpdateDto) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        if (StringUtils.hasText(profileUpdateDto.getFullName())) {
            user.setFullName(profileUpdateDto.getFullName());
        }
        if (StringUtils.hasText(profileUpdateDto.getProfileImageUrl())) {
            user.setProfileImageUrl(profileUpdateDto.getProfileImageUrl());
        }
        if (StringUtils.hasText(profileUpdateDto.getEmail()) && !profileUpdateDto.getEmail().equals(user.getEmail())) {
            user.setEmail(profileUpdateDto.getEmail());
            user.setEmailVerified(false);
            // TODO: Send verification email
        }
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return toDto(user);
    }

    @Override
    public void initiatePasswordReset(String email) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            // Do not reveal user existence
            return;
        }
        User user = userOpt.get();
        // TODO: Generate token, save, and send email
    }

    @Override
    public void resetPassword(PasswordResetDto passwordResetDto) {
        // TODO: Validate token, check expiration, reset password
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        user.setDeleted(true);
        user.setDeletedAt(LocalDateTime.now());
        userRepository.save(user);
        // TODO: Remove all user data (GDPR)
    }

    @Override
    public UserDto getUserProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User not found"));
        return toDto(user);
    }

    private UserDto toDto(User user) {
        return UserDto.builder()
                .id(user.getId())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .emailVerified(user.isEmailVerified())
                .profileImageUrl(user.getProfileImageUrl())
                .build();
    }
}

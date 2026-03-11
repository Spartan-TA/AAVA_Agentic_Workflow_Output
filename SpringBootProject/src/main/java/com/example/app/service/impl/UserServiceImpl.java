package com.example.app.service.impl;

import com.example.app.dto.*;
import com.example.app.entity.User;
import com.example.app.entity.UserPreferences;
import com.example.app.repository.UserRepository;
import com.example.app.repository.UserPreferencesRepository;
import com.example.app.service.UserService;
import com.example.app.exception.RegistrationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserPreferencesRepository preferencesRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User registerUser(UserRegistrationDto registrationDto) {
        if (userRepository.existsByUsername(registrationDto.getUsername()) || userRepository.existsByEmail(registrationDto.getEmail())) {
            throw new RegistrationException("Username or email already exists");
        }
        User user = User.builder()
                .username(registrationDto.getUsername())
                .email(registrationDto.getEmail())
                .password(passwordEncoder.encode(registrationDto.getPassword()))
                .enabled(false)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        user = userRepository.save(user);
        UserPreferences preferences = UserPreferences.builder()
                .user(user)
                .emailNotificationsEnabled(true)
                .smsNotificationsEnabled(false)
                .theme("light")
                .build();
        preferencesRepository.save(preferences);
        return user;
    }

    @Override
    public UserProfileDto getUserProfile(String username) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RegistrationException("User not found"));
        return UserProfileDto.fromEntity(user);
    }

    @Override
    @Transactional
    public UserProfileDto updateUserProfile(String username, UserProfileDto profileDto) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RegistrationException("User not found"));
        user.setEmail(profileDto.getEmail());
        user.setProfilePictureUrl(profileDto.getProfilePictureUrl());
        user.setUpdatedAt(LocalDateTime.now());
        userRepository.save(user);
        return UserProfileDto.fromEntity(user);
    }

    @Override
    public void changePassword(String username, ChangePasswordDto changePasswordDto) {
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RegistrationException("User not found"));
        user.setPassword(passwordEncoder.encode(changePasswordDto.getNewPassword()));
        userRepository.save(user);
    }

    @Override
    public void uploadProfilePicture(String username, byte[] fileData, String filename) {
        // File storage logic should be implemented here
        User user = userRepository.findByUsername(username).orElseThrow(() -> new RegistrationException("User not found"));
        user.setProfilePictureUrl("/uploads/" + filename);
        userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}

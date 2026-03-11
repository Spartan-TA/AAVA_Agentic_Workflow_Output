package com.example.app.service;

import com.example.app.dto.*;
import com.example.app.entity.User;
import java.util.Optional;

public interface UserService {
    User registerUser(UserRegistrationDto registrationDto);
    UserProfileDto getUserProfile(String username);
    UserProfileDto updateUserProfile(String username, UserProfileDto profileDto);
    void changePassword(String username, ChangePasswordDto changePasswordDto);
    void uploadProfilePicture(String username, byte[] fileData, String filename);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
}

package com.example.app.controller;

import com.example.app.dto.*;
import com.example.app.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserProfileDto> register(@RequestBody UserRegistrationDto registrationDto) {
        userService.registerUser(registrationDto);
        UserProfileDto profile = userService.getUserProfile(registrationDto.getUsername());
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(@AuthenticationPrincipal Principal principal) {
        UserProfileDto profile = userService.getUserProfile(principal.getName());
        return ResponseEntity.ok(profile);
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDto> updateProfile(@AuthenticationPrincipal Principal principal, @RequestBody UserProfileDto profileDto) {
        UserProfileDto updated = userService.updateUserProfile(principal.getName(), profileDto);
        return ResponseEntity.ok(updated);
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Principal principal, @RequestBody ChangePasswordDto changePasswordDto) {
        userService.changePassword(principal.getName(), changePasswordDto);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/profile-picture")
    public ResponseEntity<Void> uploadProfilePicture(@AuthenticationPrincipal Principal principal, @RequestParam("file") MultipartFile file) {
        try {
            userService.uploadProfilePicture(principal.getName(), file.getBytes(), file.getOriginalFilename());
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok().build();
    }
}

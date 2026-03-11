package com.example.platform.controller;

import com.example.platform.dto.UserProfileDto;
import com.example.platform.dto.UpdateProfileRequest;
import com.example.platform.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/user")
@Validated
public class UserController {
    @Autowired
    private UserService userService;

    @GetMapping("/profile")
    public ResponseEntity<UserProfileDto> getProfile(@AuthenticationPrincipal(expression = "id") Long userId) {
        return ResponseEntity.ok(userService.getUserProfile(userId));
    }

    @PutMapping("/profile")
    public ResponseEntity<Void> updateProfile(@AuthenticationPrincipal(expression = "id") Long userId,
                                              @Valid @RequestBody UpdateProfileRequest request) {
        userService.updateProfile(userId, request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal(expression = "id") Long userId,
                                               @RequestParam String oldPassword,
                                               @RequestParam String newPassword) {
        userService.changePassword(userId, oldPassword, newPassword);
        return ResponseEntity.ok().build();
    }
}

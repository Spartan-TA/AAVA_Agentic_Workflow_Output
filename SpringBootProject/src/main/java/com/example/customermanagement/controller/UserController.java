package com.example.customermanagement.controller;

import com.example.customermanagement.dto.UserDTO;
import com.example.customermanagement.model.User;
import com.example.customermanagement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * REST controller for user registration, verification, and profile update.
 */
@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * Registers a new user.
     * @param userDTO User registration data
     * @return Success message
     */
    @PostMapping("/register")
    public ResponseEntity<String> registerUser(@Valid @RequestBody UserDTO userDTO) {
        userService.registerUser(userDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully. Please verify your email.");
    }

    /**
     * Verifies a user's email with a token.
     * @param token Verification token
     * @return Success message
     */
    @GetMapping("/verify")
    public ResponseEntity<String> verifyUser(@RequestParam("token") String token) {
        userService.verifyUser(token);
        return ResponseEntity.ok("User verified successfully.");
    }

    /**
     * Updates the authenticated user's profile.
     * @param userDTO Updated user data
     * @param authentication Authenticated user
     * @return Updated user
     */
    @PutMapping("/profile")
    public ResponseEntity<User> updateProfile(@Valid @RequestBody UserDTO userDTO, Authentication authentication) {
        String username = authentication.getName();
        User updatedUser = userService.updateProfile(username, userDTO);
        return ResponseEntity.ok(updatedUser);
    }
}

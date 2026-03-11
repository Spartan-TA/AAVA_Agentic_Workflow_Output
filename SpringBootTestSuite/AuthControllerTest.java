package com.example.demo;

import com.example.demo.controller.AuthController;
import com.example.demo.dto.*;
import com.example.demo.exception.*;
import com.example.demo.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Login Endpoint")
    class LoginTests {
        @Test
        void login_withValidCredentials_returnsJwtToken() throws Exception {
            LoginRequest request = new LoginRequest("user1", "password123");
            LoginResponse response = new LoginResponse("jwt-token");
            Mockito.when(authService.login(any(LoginRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token").value("jwt-token"));
        }

        @Test
        void login_withInvalidCredentials_returnsUnauthorized() throws Exception {
            LoginRequest request = new LoginRequest("user1", "wrongpass");
            Mockito.when(authService.login(any(LoginRequest.class))).thenThrow(new InvalidCredentialsException());

            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        void login_withNullUsername_returnsBadRequest() throws Exception {
            LoginRequest request = new LoginRequest(null, "password123");
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void login_withEmptyPassword_returnsBadRequest() throws Exception {
            LoginRequest request = new LoginRequest("user1", "");
            mockMvc.perform(post("/api/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Registration Endpoint")
    class RegistrationTests {
        @Test
        void register_withValidInput_returnsCreated() throws Exception {
            RegisterRequest request = new RegisterRequest("user2", "user2@email.com", "Password1!");
            RegisterResponse response = new RegisterResponse("user2", "user2@email.com");
            Mockito.when(authService.register(any(RegisterRequest.class))).thenReturn(response);

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.username").value("user2"));
        }

        @Test
        void register_withExistingUsername_returnsConflict() throws Exception {
            RegisterRequest request = new RegisterRequest("user1", "user1@email.com", "Password1!");
            Mockito.when(authService.register(any(RegisterRequest.class))).thenThrow(new UserAlreadyExistsException());

            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isConflict());
        }

        @Test
        void register_withInvalidEmail_returnsBadRequest() throws Exception {
            RegisterRequest request = new RegisterRequest("user3", "invalid-email", "Password1!");
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void register_withShortPassword_returnsBadRequest() throws Exception {
            RegisterRequest request = new RegisterRequest("user4", "user4@email.com", "123");
            mockMvc.perform(post("/api/auth/register")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Password Reset Endpoint")
    class PasswordResetTests {
        @Test
        void resetPassword_withValidEmail_returnsOk() throws Exception {
            PasswordResetRequest request = new PasswordResetRequest("user1@email.com");
            Mockito.doNothing().when(authService).resetPassword(any(PasswordResetRequest.class));

            mockMvc.perform(post("/api/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        void resetPassword_withNonexistentEmail_returnsNotFound() throws Exception {
            PasswordResetRequest request = new PasswordResetRequest("notfound@email.com");
            Mockito.doThrow(new UserNotFoundException()).when(authService).resetPassword(any(PasswordResetRequest.class));

            mockMvc.perform(post("/api/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound());
        }

        @Test
        void resetPassword_withInvalidEmailFormat_returnsBadRequest() throws Exception {
            PasswordResetRequest request = new PasswordResetRequest("bademail");
            mockMvc.perform(post("/api/auth/reset-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }
}

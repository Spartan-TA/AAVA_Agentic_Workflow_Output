package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.entity.User;
import com.example.auth.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@ExtendWith(MockitoExtension.class)
class AuthControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @Autowired
    private ObjectMapper objectMapper;

    private LoginRequestDto loginRequest;
    private RegisterRequestDto registerRequest;
    private ProfileUpdateDto profileUpdateDto;
    private PasswordResetDto.Request passwordResetRequest;
    private PasswordResetDto.Confirm passwordResetConfirm;
    private UserDto userDto;

    @BeforeEach
    void setUp() {
        loginRequest = new LoginRequestDto();
        loginRequest.setEmail("test@example.com");
        loginRequest.setPassword("password");

        registerRequest = new RegisterRequestDto();
        registerRequest.setEmail("newuser@example.com");
        registerRequest.setPassword("Password123!");
        registerRequest.setName("New User");

        profileUpdateDto = new ProfileUpdateDto();
        profileUpdateDto.setName("Updated Name");
        profileUpdateDto.setPhone("1234567890");

        passwordResetRequest = new PasswordResetDto.Request();
        passwordResetRequest.setEmail("test@example.com");

        passwordResetConfirm = new PasswordResetDto.Confirm();
        passwordResetConfirm.setToken("valid-token");
        passwordResetConfirm.setNewPassword("NewPassword123!");

        userDto = new UserDto();
        userDto.setEmail("test@example.com");
        userDto.setName("Test User");
    }

    // LOGIN ENDPOINT
    @Test
    void testLogin_ValidRequest_ReturnsOk() throws Exception {
        when(authService.login(any(LoginRequestDto.class))).thenReturn(new AuthResponseDto("token"));
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token"));
    }

    @Test
    void testLogin_InvalidRequest_ReturnsBadRequest() throws Exception {
        loginRequest.setEmail("");
        mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isBadRequest());
    }

    // REGISTER ENDPOINT
    @Test
    void testRegister_ValidRequest_ReturnsCreated() throws Exception {
        when(authService.register(any(RegisterRequestDto.class))).thenReturn(userDto);
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    void testRegister_DuplicateEmail_ReturnsBadRequest() throws Exception {
        when(authService.register(any(RegisterRequestDto.class))).thenThrow(new IllegalArgumentException("Duplicate email"));
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isBadRequest());
    }

    // UPDATE PROFILE ENDPOINT
    @Test
    @WithMockUser
    void testUpdateProfile_ValidRequest_ReturnsOk() throws Exception {
        when(authService.updateProfile(eq(1L), any(ProfileUpdateDto.class))).thenReturn(userDto);
        mockMvc.perform(put("/api/auth/profile/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }

    @Test
    @WithMockUser
    void testUpdateProfile_UserNotFound_ReturnsNotFound() throws Exception {
        when(authService.updateProfile(eq(1L), any(ProfileUpdateDto.class))).thenThrow(new javax.persistence.EntityNotFoundException());
        mockMvc.perform(put("/api/auth/profile/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(profileUpdateDto)))
                .andExpect(status().isNotFound());
    }

    // PASSWORD RESET REQUEST ENDPOINT
    @Test
    void testRequestPasswordReset_ValidRequest_ReturnsOk() throws Exception {
        doNothing().when(authService).requestPasswordReset(any(PasswordResetDto.Request.class));
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetRequest)))
                .andExpect(status().isOk());
    }

    @Test
    void testRequestPasswordReset_InvalidRequest_ReturnsBadRequest() throws Exception {
        passwordResetRequest.setEmail("");
        mockMvc.perform(post("/api/auth/password-reset/request")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetRequest)))
                .andExpect(status().isBadRequest());
    }

    // PASSWORD RESET CONFIRM ENDPOINT
    @Test
    void testConfirmPasswordReset_ValidRequest_ReturnsOk() throws Exception {
        doNothing().when(authService).confirmPasswordReset(any(PasswordResetDto.Confirm.class));
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetConfirm)))
                .andExpect(status().isOk());
    }

    @Test
    void testConfirmPasswordReset_InvalidToken_ReturnsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Invalid token")).when(authService).confirmPasswordReset(any(PasswordResetDto.Confirm.class));
        mockMvc.perform(post("/api/auth/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(passwordResetConfirm)))
                .andExpect(status().isBadRequest());
    }

    // DELETE USER ENDPOINT
    @Test
    @WithMockUser
    void testDeleteUser_ValidRequest_ReturnsNoContent() throws Exception {
        doNothing().when(authService).deleteUser(eq(1L), eq("CONFIRM"));
        mockMvc.perform(delete("/api/auth/delete/1")
                .param("confirmation", "CONFIRM"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser
    void testDeleteUser_WrongConfirmation_ReturnsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Wrong confirmation")).when(authService).deleteUser(eq(1L), eq("WRONG"));
        mockMvc.perform(delete("/api/auth/delete/1")
                .param("confirmation", "WRONG"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser
    void testDeleteUser_UserNotFound_ReturnsNotFound() throws Exception {
        doThrow(new javax.persistence.EntityNotFoundException()).when(authService).deleteUser(eq(1L), eq("CONFIRM"));
        mockMvc.perform(delete("/api/auth/delete/1")
                .param("confirmation", "CONFIRM"))
                .andExpect(status().isNotFound());
    }
}

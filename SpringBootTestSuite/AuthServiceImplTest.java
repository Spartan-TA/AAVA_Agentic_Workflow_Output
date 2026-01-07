package com.example.auth.service.impl;

import com.example.auth.dto.*;
import com.example.auth.entity.User;
import com.example.auth.repository.UserRepository;
import com.example.auth.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceImplTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private AuthServiceImpl authService;

    private User testUser;
    private LoginRequestDto loginRequest;
    private RegisterRequestDto registerRequest;
    private ProfileUpdateDto profileUpdateDto;
    private PasswordResetDto.Request passwordResetRequest;
    private PasswordResetDto.Confirm passwordResetConfirm;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setFailedLoginAttempts(0);
        testUser.setAccountLocked(false);
        testUser.setDeleted(false);
        testUser.setResetToken("valid-token");
        testUser.setResetTokenExpiry(LocalDateTime.now().plusHours(1));

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
    }

    // LOGIN TESTS
    @Test
    void testLogin_ValidCredentials_ReturnsAuthResponse() {
        when(userRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        var response = authService.login(loginRequest);
        assertNotNull(response);
        verify(userRepository).findByEmailAndDeletedFalse(loginRequest.getEmail());
    }

    @Test
    void testLogin_InvalidPassword_IncrementsFailedAttempts() {
        when(userRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
        assertEquals(1, testUser.getFailedLoginAttempts());
    }

    @Test
    void testLogin_AccountLocked_ThrowsLockedException() {
        testUser.setAccountLocked(true);
        when(userRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        assertThrows(LockedException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_FailedAttemptsAtBoundary_LocksAccount() {
        testUser.setFailedLoginAttempts(4);
        when(userRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);
        assertThrows(LockedException.class, () -> authService.login(loginRequest));
        assertTrue(testUser.isAccountLocked());
    }

    @Test
    void testLogin_UserNotFound_ThrowsUsernameNotFoundException() {
        when(userRepository.findByEmailAndDeletedFalse(loginRequest.getEmail())).thenReturn(Optional.empty());
        assertThrows(UsernameNotFoundException.class, () -> authService.login(loginRequest));
    }

    @Test
    void testLogin_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> authService.login(null));
    }

    @Test
    void testLogin_EmptyEmail_ThrowsIllegalArgumentException() {
        loginRequest.setEmail("");
        assertThrows(IllegalArgumentException.class, () -> authService.login(loginRequest));
    }

    // REGISTER TESTS
    @Test
    void testRegister_ValidRequest_CreatesUser() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = authService.register(registerRequest);
        assertNotNull(response);
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_DuplicateEmail_ThrowsIllegalArgumentException() {
        when(userRepository.existsByEmail(registerRequest.getEmail())).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
    }

    @Test
    void testRegister_InvalidEmailFormat_ThrowsIllegalArgumentException() {
        registerRequest.setEmail("invalid-email");
        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
    }

    @Test
    void testRegister_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> authService.register(null));
    }

    @Test
    void testRegister_EmptyPassword_ThrowsIllegalArgumentException() {
        registerRequest.setPassword("");
        assertThrows(IllegalArgumentException.class, () -> authService.register(registerRequest));
    }

    // UPDATE PROFILE TESTS
    @Test
    void testUpdateProfile_ValidRequest_UpdatesUser() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        var response = authService.updateProfile(1L, profileUpdateDto);
        assertNotNull(response);
        assertEquals("Updated Name", testUser.getName());
        verify(userRepository).save(testUser);
    }

    @Test
    void testUpdateProfile_UserNotFound_ThrowsEntityNotFoundException() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());
        assertThrows(javax.persistence.EntityNotFoundException.class, () -> authService.updateProfile(1L, profileUpdateDto));
    }

    @Test
    void testUpdateProfile_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> authService.updateProfile(1L, null));
    }

    // PASSWORD RESET REQUEST TESTS
    @Test
    void testRequestPasswordReset_ValidEmail_SendsReset() {
        when(userRepository.findByEmailAndDeletedFalse(passwordResetRequest.getEmail())).thenReturn(Optional.of(testUser));
        authService.requestPasswordReset(passwordResetRequest);
        assertNotNull(testUser.getResetToken());
        assertNotNull(testUser.getResetTokenExpiry());
        verify(userRepository).save(testUser);
    }

    @Test
    void testRequestPasswordReset_UserNotFound_NoException() {
        when(userRepository.findByEmailAndDeletedFalse(passwordResetRequest.getEmail())).thenReturn(Optional.empty());
        assertDoesNotThrow(() -> authService.requestPasswordReset(passwordResetRequest));
    }

    @Test
    void testRequestPasswordReset_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> authService.requestPasswordReset(null));
    }

    // PASSWORD RESET CONFIRM TESTS
    @Test
    void testConfirmPasswordReset_ValidToken_ResetsPassword() {
        when(userRepository.findByResetToken("valid-token")).thenReturn(Optional.of(testUser));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedNewPassword");
        authService.confirmPasswordReset(passwordResetConfirm);
        assertNull(testUser.getResetToken());
        assertNull(testUser.getResetTokenExpiry());
        verify(userRepository).save(testUser);
    }

    @Test
    void testConfirmPasswordReset_InvalidToken_ThrowsIllegalArgumentException() {
        when(userRepository.findByResetToken("invalid-token")).thenReturn(Optional.empty());
        passwordResetConfirm.setToken("invalid-token");
        assertThrows(IllegalArgumentException.class, () -> authService.confirmPasswordReset(passwordResetConfirm));
    }

    @Test
    void testConfirmPasswordReset_ExpiredToken_ThrowsIllegalArgumentException() {
        testUser.setResetTokenExpiry(LocalDateTime.now().minusMinutes(1));
        when(userRepository.findByResetToken("valid-token")).thenReturn(Optional.of(testUser));
        assertThrows(IllegalArgumentException.class, () -> authService.confirmPasswordReset(passwordResetConfirm));
    }

    @Test
    void testConfirmPasswordReset_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> authService.confirmPasswordReset(null));
    }

    // DELETE USER TESTS
    @Test
    void testDeleteUser_ValidConfirmation_DeletesUser() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        authService.deleteUser(1L, "CONFIRM");
        assertTrue(testUser.isDeleted());
        verify(userRepository).save(testUser);
    }

    @Test
    void testDeleteUser_WrongConfirmation_ThrowsIllegalArgumentException() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.of(testUser));
        assertThrows(IllegalArgumentException.class, () -> authService.deleteUser(1L, "WRONG"));
    }

    @Test
    void testDeleteUser_UserNotFound_ThrowsEntityNotFoundException() {
        when(userRepository.findByIdAndDeletedFalse(1L)).thenReturn(Optional.empty());
        assertThrows(javax.persistence.EntityNotFoundException.class, () -> authService.deleteUser(1L, "CONFIRM"));
    }

    // CONVERT TO DTO TESTS
    @Test
    void testConvertToDto_ValidUser_ReturnsDto() {
        var dto = authService.convertToDto(testUser);
        assertNotNull(dto);
        assertEquals(testUser.getEmail(), dto.getEmail());
    }

    @Test
    void testConvertToDto_NullUser_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> authService.convertToDto(null));
    }
}

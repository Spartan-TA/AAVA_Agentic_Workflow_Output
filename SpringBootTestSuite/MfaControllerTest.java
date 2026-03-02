package com.example.usermanagement.controller;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.service.MfaService;
import com.example.usermanagement.controller.MfaController;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for MfaController covering MFA setup, verification, and edge cases.
 */
public class MfaControllerTest {
    @Mock private MfaService mfaService;
    @InjectMocks private MfaController mfaController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetupMfa_ValidUser_Success() {
        User user = new User();
        when(mfaService.setupMfa(any(User.class))).thenReturn("SECRET123");
        ResponseEntity<?> response = mfaController.setupMfa(user);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals("SECRET123", response.getBody());
    }

    @Test
    void testSetupMfa_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> mfaController.setupMfa(null));
    }

    @Test
    void testVerifyMfaCode_Valid_Success() {
        User user = new User();
        when(mfaService.verifyMfaCode(any(User.class), anyString())).thenReturn(true);
        ResponseEntity<?> response = mfaController.verifyMfaCode(user, "123456");
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(true, response.getBody());
    }

    @Test
    void testVerifyMfaCode_Invalid_ThrowsException() {
        User user = new User();
        when(mfaService.verifyMfaCode(any(User.class), anyString())).thenThrow(new RuntimeException("Invalid code"));
        assertThrows(RuntimeException.class, () -> mfaController.verifyMfaCode(user, "000000"));
    }

    @Test
    void testVerifyMfaCode_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> mfaController.verifyMfaCode(null, "123456"));
    }

    @Test
    void testVerifyMfaCode_NullCode_ThrowsException() {
        User user = new User();
        assertThrows(IllegalArgumentException.class, () -> mfaController.verifyMfaCode(user, null));
    }
}

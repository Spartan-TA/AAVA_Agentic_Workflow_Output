package com.example.usermanagement.service;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.exception.InvalidMfaCodeException;
import com.example.usermanagement.service.impl.MfaServiceImpl;
import com.example.usermanagement.util.TokenGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for MfaServiceImpl covering TOTP setup, verification, validation, and edge cases.
 */
public class MfaServiceImplTest {
    @Mock private TokenGenerator tokenGenerator;
    @InjectMocks private MfaServiceImpl mfaService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSetupMfa_ValidUser_Success() {
        User user = new User();
        when(tokenGenerator.generateTotpSecret()).thenReturn("SECRET123");
        String secret = mfaService.setupMfa(user);
        assertEquals("SECRET123", secret);
        assertEquals("SECRET123", user.getMfaSecret());
    }

    @Test
    void testSetupMfa_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> mfaService.setupMfa(null));
    }

    @Test
    void testVerifyMfaCode_ValidCode_Success() {
        User user = new User();
        user.setMfaSecret("SECRET123");
        when(tokenGenerator.verifyTotpCode(eq("SECRET123"), eq("123456"))).thenReturn(true);
        assertTrue(mfaService.verifyMfaCode(user, "123456"));
    }

    @Test
    void testVerifyMfaCode_InvalidCode_ThrowsException() {
        User user = new User();
        user.setMfaSecret("SECRET123");
        when(tokenGenerator.verifyTotpCode(eq("SECRET123"), eq("000000"))).thenReturn(false);
        assertThrows(InvalidMfaCodeException.class, () -> mfaService.verifyMfaCode(user, "000000"));
    }

    @Test
    void testVerifyMfaCode_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> mfaService.verifyMfaCode(null, "123456"));
    }

    @Test
    void testVerifyMfaCode_NullCode_ThrowsException() {
        User user = new User();
        user.setMfaSecret("SECRET123");
        assertThrows(IllegalArgumentException.class, () -> mfaService.verifyMfaCode(user, null));
    }

    @Test
    void testVerifyMfaCode_EmptyCode_ThrowsException() {
        User user = new User();
        user.setMfaSecret("SECRET123");
        assertThrows(IllegalArgumentException.class, () -> mfaService.verifyMfaCode(user, ""));
    }
}

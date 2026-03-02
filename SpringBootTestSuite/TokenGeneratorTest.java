package com.example.usermanagement.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for TokenGenerator covering TOTP secret and code generation, and edge cases.
 */
public class TokenGeneratorTest {
    @Test
    void testGenerateTotpSecret_NotNullOrEmpty() {
        String secret = TokenGenerator.generateTotpSecret();
        assertNotNull(secret);
        assertTrue(secret.length() >= 16);
    }

    @Test
    void testGenerateTotpCode_ValidSecret() {
        String secret = TokenGenerator.generateTotpSecret();
        String code = TokenGenerator.generateTotpCode(secret);
        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\d{6}"));
    }

    @Test
    void testGenerateTotpCode_NullSecret_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> TokenGenerator.generateTotpCode(null));
    }

    @Test
    void testGenerateTotpCode_EmptySecret_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> TokenGenerator.generateTotpCode(""));
    }
}

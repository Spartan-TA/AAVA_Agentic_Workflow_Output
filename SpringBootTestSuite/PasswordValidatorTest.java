package com.example.usermanagement.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for PasswordValidator covering password validation logic and edge cases.
 */
public class PasswordValidatorTest {
    @Test
    void testIsValid_ValidPassword_ReturnsTrue() {
        assertTrue(PasswordValidator.isValid("Password1"));
        assertTrue(PasswordValidator.isValid("Abcdefg1"));
    }

    @Test
    void testIsValid_ShortPassword_ReturnsFalse() {
        assertFalse(PasswordValidator.isValid("Abc1"));
    }

    @Test
    void testIsValid_NoUppercase_ReturnsFalse() {
        assertFalse(PasswordValidator.isValid("password1"));
    }

    @Test
    void testIsValid_NoNumber_ReturnsFalse() {
        assertFalse(PasswordValidator.isValid("Password"));
    }

    @Test
    void testIsValid_EmptyString_ReturnsFalse() {
        assertFalse(PasswordValidator.isValid(""));
    }

    @Test
    void testIsValid_Null_ReturnsFalse() {
        assertFalse(PasswordValidator.isValid(null));
    }
}

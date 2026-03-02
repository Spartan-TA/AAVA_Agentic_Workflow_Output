package com.example.usermanagement.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive unit tests for EmailValidator covering email validation logic and edge cases.
 */
public class EmailValidatorTest {
    @Test
    void testIsValid_ValidEmail_ReturnsTrue() {
        assertTrue(EmailValidator.isValid("test@example.com"));
        assertTrue(EmailValidator.isValid("user.name+tag@domain.co"));
    }

    @Test
    void testIsValid_InvalidEmail_ReturnsFalse() {
        assertFalse(EmailValidator.isValid("plainaddress"));
        assertFalse(EmailValidator.isValid("@missingusername.com"));
        assertFalse(EmailValidator.isValid("username@.com"));
        assertFalse(EmailValidator.isValid("username@domain"));
    }

    @Test
    void testIsValid_EmptyString_ReturnsFalse() {
        assertFalse(EmailValidator.isValid(""));
    }

    @Test
    void testIsValid_Null_ReturnsFalse() {
        assertFalse(EmailValidator.isValid(null));
    }
}

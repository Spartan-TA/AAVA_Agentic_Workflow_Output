package com.example.auth.entity;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setFailedLoginAttempts(0);
        user.setAccountLocked(false);
        user.setLockTime(null);
    }

    @Test
    void testIncrementFailedAttempts_IncrementsByOne() {
        user.incrementFailedAttempts();
        assertEquals(1, user.getFailedLoginAttempts());
        user.incrementFailedAttempts();
        assertEquals(2, user.getFailedLoginAttempts());
    }

    @Test
    void testResetFailedAttempts_SetsToZero() {
        user.setFailedLoginAttempts(3);
        user.resetFailedAttempts();
        assertEquals(0, user.getFailedLoginAttempts());
    }

    @Test
    void testLockAccount_SetsLockedAndLockTime() {
        assertFalse(user.isAccountLocked());
        assertNull(user.getLockTime());
        user.lockAccount();
        assertTrue(user.isAccountLocked());
        assertNotNull(user.getLockTime());
        assertTrue(user.getLockTime().isBefore(LocalDateTime.now().plusSeconds(1)));
    }

    @Test
    void testUnlockAccount_ResetsLockedAndLockTime() {
        user.lockAccount();
        assertTrue(user.isAccountLocked());
        user.unlockAccount();
        assertFalse(user.isAccountLocked());
        assertNull(user.getLockTime());
    }

    @Test
    void testIncrementFailedAttempts_BoundaryCondition() {
        user.setFailedLoginAttempts(4);
        user.incrementFailedAttempts();
        assertEquals(5, user.getFailedLoginAttempts());
    }

    @Test
    void testLockAccount_Idempotent() {
        user.lockAccount();
        LocalDateTime firstLockTime = user.getLockTime();
        user.lockAccount();
        assertEquals(firstLockTime, user.getLockTime()); // Should not reset lock time
    }

    @Test
    void testUnlockAccount_WhenNotLocked() {
        user.unlockAccount();
        assertFalse(user.isAccountLocked());
        assertNull(user.getLockTime());
    }
}

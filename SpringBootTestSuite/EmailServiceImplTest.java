package com.example.usermanagement.service;

import com.example.usermanagement.entity.User;
import com.example.usermanagement.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for EmailServiceImpl covering email sending and edge cases.
 */
public class EmailServiceImplTest {
    @Mock private JavaMailSender mailSender;
    @InjectMocks private EmailServiceImpl emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendVerificationEmail_ValidInput_Success() {
        User user = new User();
        user.setEmail("test@example.com");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.sendVerificationEmail(user, "token123"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendVerificationEmail_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> emailService.sendVerificationEmail(null, "token"));
    }

    @Test
    void testSendVerificationEmail_NullToken_ThrowsException() {
        User user = new User();
        user.setEmail("test@example.com");
        assertThrows(IllegalArgumentException.class, () -> emailService.sendVerificationEmail(user, null));
    }

    @Test
    void testSendVerificationEmail_EmptyToken_ThrowsException() {
        User user = new User();
        user.setEmail("test@example.com");
        assertThrows(IllegalArgumentException.class, () -> emailService.sendVerificationEmail(user, ""));
    }

    @Test
    void testSendPasswordResetEmail_ValidInput_Success() {
        User user = new User();
        user.setEmail("reset@example.com");
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail(user, "resetToken"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendPasswordResetEmail_NullUser_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> emailService.sendPasswordResetEmail(null, "token"));
    }

    @Test
    void testSendPasswordResetEmail_NullToken_ThrowsException() {
        User user = new User();
        user.setEmail("reset@example.com");
        assertThrows(IllegalArgumentException.class, () -> emailService.sendPasswordResetEmail(user, null));
    }
}

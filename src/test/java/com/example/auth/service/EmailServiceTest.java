package com.example.auth.service;

import com.example.auth.entity.User;
import com.example.auth.exception.EmailSendException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    private AutoCloseable closeable;
    private User testUser;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testSendPasswordResetEmail_ValidUserAndToken_Success() {
        doNothing().when(mailSender).send(any(SimpleMailMessage.class));
        assertDoesNotThrow(() -> emailService.sendPasswordResetEmail(testUser, "token123"));
        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void testSendPasswordResetEmail_NullUser_ThrowsException() {
        assertThrows(EmailSendException.class, () -> emailService.sendPasswordResetEmail(null, "token123"));
    }

    @Test
    void testSendPasswordResetEmail_NullToken_ThrowsException() {
        assertThrows(EmailSendException.class, () -> emailService.sendPasswordResetEmail(testUser, null));
    }

    @Test
    void testSendPasswordResetEmail_EmptyToken_ThrowsException() {
        assertThrows(EmailSendException.class, () -> emailService.sendPasswordResetEmail(testUser, ""));
    }

    @Test
    void testSendPasswordResetEmail_NullUserEmail_ThrowsException() {
        testUser.setEmail(null);
        assertThrows(EmailSendException.class, () -> emailService.sendPasswordResetEmail(testUser, "token123"));
    }

    @Test
    void testSendPasswordResetEmail_EmptyUserEmail_ThrowsException() {
        testUser.setEmail("");
        assertThrows(EmailSendException.class, () -> emailService.sendPasswordResetEmail(testUser, "token123"));
    }

    @Test
    void testSendPasswordResetEmail_MailSenderThrowsException_ThrowsEmailSendException() {
        doThrow(new RuntimeException("Mail server error")).when(mailSender).send(any(SimpleMailMessage.class));
        assertThrows(EmailSendException.class, () -> emailService.sendPasswordResetEmail(testUser, "token123"));
    }
}

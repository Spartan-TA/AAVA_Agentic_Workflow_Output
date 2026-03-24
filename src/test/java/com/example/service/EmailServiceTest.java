package com.example.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class EmailServiceTest {

    @Mock
    private EmailSender emailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendEmailSuccess() {
        doNothing().when(emailSender).send("test@example.com", "Subject", "Body");
        boolean result = emailService.sendEmail("test@example.com", "Subject", "Body");
        assertTrue(result);
        verify(emailSender).send("test@example.com", "Subject", "Body");
    }

    @Test
    void testSendEmailFailure() {
        doThrow(new RuntimeException("Failed")).when(emailSender).send("fail@example.com", "Subject", "Body");
        boolean result = emailService.sendEmail("fail@example.com", "Subject", "Body");
        assertFalse(result);
        verify(emailSender).send("fail@example.com", "Subject", "Body");
    }

    @Test
    void testSendEmailWithNullRecipient() {
        boolean result = emailService.sendEmail(null, "Subject", "Body");
        assertFalse(result);
        verify(emailSender, never()).send(any(), any(), any());
    }

    @Test
    void testSendEmailWithEmptySubject() {
        doNothing().when(emailSender).send("test@example.com", "", "Body");
        boolean result = emailService.sendEmail("test@example.com", "", "Body");
        assertTrue(result);
        verify(emailSender).send("test@example.com", "", "Body");
    }

    @Test
    void testSendEmailWithEmptyBody() {
        doNothing().when(emailSender).send("test@example.com", "Subject", "");
        boolean result = emailService.sendEmail("test@example.com", "Subject", "");
        assertTrue(result);
        verify(emailSender).send("test@example.com", "Subject", "");
    }
}

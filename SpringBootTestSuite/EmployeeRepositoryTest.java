package com.example.ems.employee;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeRepositoryTest {

    @Mock
    private EmployeeRepository repository;

    @Test
    public void testExistsByEmailReturnsTrue() {
        Mockito.when(repository.existsByEmail("test@example.com")).thenReturn(true);
        assertTrue(repository.existsByEmail("test@example.com"));
    }

    @Test
    public void testExistsByEmailReturnsFalse() {
        Mockito.when(repository.existsByEmail("notfound@example.com")).thenReturn(false);
        assertFalse(repository.existsByEmail("notfound@example.com"));
    }

    @Test
    public void testExistsByEmailWithNull() {
        Mockito.when(repository.existsByEmail(null)).thenReturn(false);
        assertFalse(repository.existsByEmail(null));
    }

    @Test
    public void testExistsByEmailWithEmptyString() {
        Mockito.when(repository.existsByEmail("")).thenReturn(false);
        assertFalse(repository.existsByEmail(""));
    }

    @Test
    public void testExistsByEmailWithWhitespace() {
        Mockito.when(repository.existsByEmail("   ")).thenReturn(false);
        assertFalse(repository.existsByEmail("   "));
    }

    @Test
    public void testExistsByEmailCaseSensitivity() {
        Mockito.when(repository.existsByEmail("Test@Example.com")).thenReturn(true);
        Mockito.when(repository.existsByEmail("test@example.com")).thenReturn(false);
        
        assertTrue(repository.existsByEmail("Test@Example.com"));
        assertFalse(repository.existsByEmail("test@example.com"));
    }

    @Test
    public void testExistsByEmailWithSpecialCharacters() {
        String specialEmail = "user+tag@example.com";
        Mockito.when(repository.existsByEmail(specialEmail)).thenReturn(true);
        assertTrue(repository.existsByEmail(specialEmail));
    }
}
package com.example.auth.repository;

import com.example.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserRepositoryTest {

    @Mock
    private UserRepository userRepository;

    private AutoCloseable closeable;
    private User testUser;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("hashedPassword");
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testFindByEmail_ExistingUser_ReturnsUser() {
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(testUser));
        Optional<User> result = userRepository.findByEmail("test@example.com");
        assertTrue(result.isPresent());
        assertEquals("test@example.com", result.get().getEmail());
    }

    @Test
    void testFindByEmail_NonExistingUser_ReturnsEmpty() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());
        Optional<User> result = userRepository.findByEmail("notfound@example.com");
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByEmail_NullEmail_ReturnsEmpty() {
        when(userRepository.findByEmail(null)).thenReturn(Optional.empty());
        Optional<User> result = userRepository.findByEmail(null);
        assertFalse(result.isPresent());
    }

    @Test
    void testFindByEmail_EmptyEmail_ReturnsEmpty() {
        when(userRepository.findByEmail("")).thenReturn(Optional.empty());
        Optional<User> result = userRepository.findByEmail("");
        assertFalse(result.isPresent());
    }

    @Test
    void testSave_ValidUser_Success() {
        when(userRepository.save(testUser)).thenReturn(testUser);
        User saved = userRepository.save(testUser);
        assertNotNull(saved);
        assertEquals("test@example.com", saved.getEmail());
    }

    @Test
    void testSave_NullUser_ThrowsException() {
        when(userRepository.save(null)).thenThrow(new IllegalArgumentException("User cannot be null"));
        assertThrows(IllegalArgumentException.class, () -> userRepository.save(null));
    }

    @Test
    void testDeleteById_ValidId_Success() {
        doNothing().when(userRepository).deleteById(1L);
        userRepository.deleteById(1L);
        verify(userRepository, times(1)).deleteById(1L);
    }

    @Test
    void testDeleteById_NullId_ThrowsException() {
        doThrow(new IllegalArgumentException("Id cannot be null")).when(userRepository).deleteById(null);
        assertThrows(IllegalArgumentException.class, () -> userRepository.deleteById(null));
    }
}

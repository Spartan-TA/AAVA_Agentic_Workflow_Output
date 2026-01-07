package com.example.auth.repository;

import com.example.auth.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("repo@example.com");
        user.setPassword("password");
        user.setDeleted(false);
        user.setAccountLocked(false);
        user = userRepository.save(user);
    }

    @Test
    void testFindByEmail_Found() {
        Optional<User> found = userRepository.findByEmail("repo@example.com");
        assertTrue(found.isPresent());
        assertEquals("repo@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmail_NotFound() {
        Optional<User> found = userRepository.findByEmail("notfound@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testExistsByEmail_True() {
        assertTrue(userRepository.existsByEmail("repo@example.com"));
    }

    @Test
    void testExistsByEmail_False() {
        assertFalse(userRepository.existsByEmail("absent@example.com"));
    }

    @Test
    void testFindByIdAndDeletedFalse_Found() {
        Optional<User> found = userRepository.findByIdAndDeletedFalse(user.getId());
        assertTrue(found.isPresent());
        assertEquals(user.getId(), found.get().getId());
    }

    @Test
    void testFindByIdAndDeletedFalse_Deleted() {
        user.setDeleted(true);
        userRepository.save(user);
        Optional<User> found = userRepository.findByIdAndDeletedFalse(user.getId());
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByIdAndDeletedFalse_NotFound() {
        Optional<User> found = userRepository.findByIdAndDeletedFalse(999L);
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmailAndDeletedFalse_Found() {
        Optional<User> found = userRepository.findByEmailAndDeletedFalse("repo@example.com");
        assertTrue(found.isPresent());
        assertEquals("repo@example.com", found.get().getEmail());
    }

    @Test
    void testFindByEmailAndDeletedFalse_Deleted() {
        user.setDeleted(true);
        userRepository.save(user);
        Optional<User> found = userRepository.findByEmailAndDeletedFalse("repo@example.com");
        assertFalse(found.isPresent());
    }

    @Test
    void testFindByEmailAndDeletedFalse_NotFound() {
        Optional<User> found = userRepository.findByEmailAndDeletedFalse("absent@example.com");
        assertFalse(found.isPresent());
    }
}

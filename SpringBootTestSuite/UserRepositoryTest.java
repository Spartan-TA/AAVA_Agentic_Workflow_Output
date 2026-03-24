package com.example.repository;

import com.example.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void testSaveAndFindByUsername() {
        User user = new User("alice", "pass");
        userRepository.save(user);
        User found = userRepository.findByUsername("alice");
        assertNotNull(found);
        assertEquals("alice", found.getUsername());
    }

    @Test
    void testFindByUsernameNotFound() {
        User found = userRepository.findByUsername("bob");
        assertNull(found);
    }

    @Test
    void testDeleteUser() {
        User user = new User("charlie", "pass");
        userRepository.save(user);
        userRepository.delete(user);
        User found = userRepository.findByUsername("charlie");
        assertNull(found);
    }

    @Test
    void testSaveDuplicateUsername() {
        User user1 = new User("dave", "pass1");
        User user2 = new User("dave", "pass2");
        userRepository.save(user1);
        assertThrows(Exception.class, () -> userRepository.save(user2));
    }

    @Test
    void testFindById() {
        User user = new User("eve", "pass");
        user = userRepository.save(user);
        Optional<User> found = userRepository.findById(user.getId());
        assertTrue(found.isPresent());
        assertEquals("eve", found.get().getUsername());
    }
}
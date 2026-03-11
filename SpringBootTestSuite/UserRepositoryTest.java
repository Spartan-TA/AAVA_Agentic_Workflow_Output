import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class UserRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    public void setUp() {
        userRepository.deleteAll();
        user1 = new User("user1", "user1@email.com", "pass1");
        user2 = new User("user2", "user2@email.com", "pass2");
        userRepository.save(user1);
        userRepository.save(user2);
    }

    @Test
    public void testFindByEmail_Found() {
        Optional<User> found = userRepository.findByEmail("user1@email.com");
        assertTrue(found.isPresent());
        assertEquals("user1", found.get().getUsername());
    }

    @Test
    public void testFindByEmail_NotFound() {
        Optional<User> found = userRepository.findByEmail("notfound@email.com");
        assertFalse(found.isPresent());
    }

    @Test
    public void testFindById_Found() {
        Optional<User> found = userRepository.findById(user1.getId());
        assertTrue(found.isPresent());
        assertEquals("user1", found.get().getUsername());
    }

    @Test
    public void testFindById_NotFound() {
        Optional<User> found = userRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    public void testSaveAndDelete() {
        User user = new User("user3", "user3@email.com", "pass3");
        User saved = userRepository.save(user);
        assertNotNull(saved.getId());
        userRepository.delete(saved);
        assertFalse(userRepository.findById(saved.getId()).isPresent());
    }

    @Test
    public void testFindAll() {
        List<User> users = userRepository.findAll();
        assertEquals(2, users.size());
    }

    @Test
    public void testDeleteById() {
        userRepository.deleteById(user1.getId());
        assertFalse(userRepository.findById(user1.getId()).isPresent());
    }

    @Test
    public void testSave_NullFields() {
        User user = new User(null, null, null);
        assertThrows(Exception.class, () -> userRepository.save(user));
    }
}

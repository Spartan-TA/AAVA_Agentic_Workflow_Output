package SpringBootTestSuite;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.SearchServiceImpl;
import com.example.demo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

public class SearchServiceImplTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SearchServiceImpl searchService;

    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        user = new User();
        user.setId(1L);
        user.setUsername("searchuser");
        user.setEmail("search@example.com");
    }

    @Test
    void testSearchUsersByUsername_HappyPath() {
        when(userRepository.findByUsernameContainingIgnoreCase("search")).thenReturn(Arrays.asList(user));
        List<User> users = searchService.searchUsersByUsername("search");
        assertEquals(1, users.size());
    }

    @Test
    void testSearchUsersByUsername_EmptyResult() {
        when(userRepository.findByUsernameContainingIgnoreCase("none")).thenReturn(Collections.emptyList());
        List<User> users = searchService.searchUsersByUsername("none");
        assertTrue(users.isEmpty());
    }

    @Test
    void testSearchUsersByUsername_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> searchService.searchUsersByUsername(null));
    }

    @Test
    void testSearchUsersByUsername_EmptyString() {
        assertThrows(IllegalArgumentException.class, () -> searchService.searchUsersByUsername(""));
    }

    @Test
    void testSearchUsersByUsername_Exception() {
        when(userRepository.findByUsernameContainingIgnoreCase(anyString())).thenThrow(new RuntimeException("DB error"));
        assertThrows(RuntimeException.class, () -> searchService.searchUsersByUsername("search"));
    }
}
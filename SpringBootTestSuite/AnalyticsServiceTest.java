import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class AnalyticsServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private LikeRepository likeRepository;
    @InjectMocks
    private AnalyticsService analyticsService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetTotalPosts() {
        when(postRepository.count()).thenReturn(100L);
        long count = analyticsService.getTotalPosts();
        assertEquals(100L, count);
    }

    @Test
    public void testGetTotalUsers() {
        when(userRepository.count()).thenReturn(50L);
        long count = analyticsService.getTotalUsers();
        assertEquals(50L, count);
    }

    @Test
    public void testGetTotalLikes() {
        when(likeRepository.count()).thenReturn(200L);
        long count = analyticsService.getTotalLikes();
        assertEquals(200L, count);
    }

    @Test
    public void testGetMostActiveUser() {
        User user = new User("active", "active@email.com", "pass");
        when(userRepository.findMostActiveUser()).thenReturn(Optional.of(user));
        Optional<User> result = analyticsService.getMostActiveUser();
        assertTrue(result.isPresent());
        assertEquals("active", result.get().getUsername());
    }

    @Test
    public void testGetMostActiveUser_None() {
        when(userRepository.findMostActiveUser()).thenReturn(Optional.empty());
        Optional<User> result = analyticsService.getMostActiveUser();
        assertFalse(result.isPresent());
    }
}

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class SearchServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private SearchService searchService;

    private Post post;
    private User user;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        post = new Post(1L, "Title", "Content", 1L);
        user = new User("user", "user@email.com", "pass");
    }

    @Test
    public void testSearchPostsByKeyword_Found() {
        List<Post> posts = Arrays.asList(post);
        when(postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase("test", "test")).thenReturn(posts);
        List<Post> result = searchService.searchPostsByKeyword("test");
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchPostsByKeyword_NotFound() {
        when(postRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase("none", "none")).thenReturn(Collections.emptyList());
        List<Post> result = searchService.searchPostsByKeyword("none");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSearchUsersByUsername_Found() {
        List<User> users = Arrays.asList(user);
        when(userRepository.findByUsernameContainingIgnoreCase("user")).thenReturn(users);
        List<User> result = searchService.searchUsersByUsername("user");
        assertEquals(1, result.size());
    }

    @Test
    public void testSearchUsersByUsername_NotFound() {
        when(userRepository.findByUsernameContainingIgnoreCase("none")).thenReturn(Collections.emptyList());
        List<User> result = searchService.searchUsersByUsername("none");
        assertTrue(result.isEmpty());
    }

    @Test
    public void testSearchPostsByKeyword_Null() {
        assertThrows(IllegalArgumentException.class, () -> searchService.searchPostsByKeyword(null));
    }

    @Test
    public void testSearchUsersByUsername_Null() {
        assertThrows(IllegalArgumentException.class, () -> searchService.searchUsersByUsername(null));
    }
}

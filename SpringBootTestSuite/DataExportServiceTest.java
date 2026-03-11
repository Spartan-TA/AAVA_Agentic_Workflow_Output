import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.io.*;
import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DataExportServiceTest {
    @Mock
    private UserRepository userRepository;
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private DataExportService dataExportService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testExportUsersToCSV_Success() throws IOException {
        List<User> users = Arrays.asList(new User("u1", "u1@email.com", "p1"), new User("u2", "u2@email.com", "p2"));
        when(userRepository.findAll()).thenReturn(users);
        String csv = dataExportService.exportUsersToCSV();
        assertTrue(csv.contains("u1@email.com"));
        assertTrue(csv.contains("u2@email.com"));
    }

    @Test
    public void testExportPostsToCSV_Success() throws IOException {
        List<Post> posts = Arrays.asList(new Post(1L, "T1", "C1", 1L), new Post(2L, "T2", "C2", 2L));
        when(postRepository.findAll()).thenReturn(posts);
        String csv = dataExportService.exportPostsToCSV();
        assertTrue(csv.contains("T1"));
        assertTrue(csv.contains("T2"));
    }

    @Test
    public void testExportCommentsToCSV_Success() throws IOException {
        List<Comment> comments = Arrays.asList(new Comment(1L, 1L, "C1", 1L), new Comment(2L, 2L, "C2", 2L));
        when(commentRepository.findAll()).thenReturn(comments);
        String csv = dataExportService.exportCommentsToCSV();
        assertTrue(csv.contains("C1"));
        assertTrue(csv.contains("C2"));
    }

    @Test
    public void testExportUsersToCSV_Empty() throws IOException {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());
        String csv = dataExportService.exportUsersToCSV();
        assertTrue(csv.isEmpty() || csv.equals("header
"));
    }

    @Test
    public void testExportPostsToCSV_Empty() throws IOException {
        when(postRepository.findAll()).thenReturn(Collections.emptyList());
        String csv = dataExportService.exportPostsToCSV();
        assertTrue(csv.isEmpty() || csv.equals("header
"));
    }

    @Test
    public void testExportCommentsToCSV_Empty() throws IOException {
        when(commentRepository.findAll()).thenReturn(Collections.emptyList());
        String csv = dataExportService.exportCommentsToCSV();
        assertTrue(csv.isEmpty() || csv.equals("header
"));
    }
}

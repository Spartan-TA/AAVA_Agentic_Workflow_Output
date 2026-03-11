import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class PostServiceTest {
    @Mock
    private PostRepository postRepository;
    @InjectMocks
    private PostService postService;

    private Post post;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        post = new Post(1L, "Title", "Content", 1L);
    }

    @Test
    public void testCreatePost_Success() {
        when(postRepository.save(any(Post.class))).thenReturn(post);
        Post result = postService.createPost(post);
        assertEquals(post, result);
    }

    @Test
    public void testGetPostById_Found() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        Post result = postService.getPostById(1L);
        assertEquals(post, result);
    }

    @Test
    public void testGetPostById_NotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> postService.getPostById(1L));
    }

    @Test
    public void testUpdatePost_Success() {
        Post updated = new Post(1L, "NewTitle", "NewContent", 1L);
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(updated);
        Post result = postService.updatePost(1L, updated);
        assertEquals("NewTitle", result.getTitle());
    }

    @Test
    public void testUpdatePost_NotFound() {
        Post updated = new Post(1L, "NewTitle", "NewContent", 1L);
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> postService.updatePost(1L, updated));
    }

    @Test
    public void testDeletePost_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        doNothing().when(postRepository).deleteById(1L);
        assertDoesNotThrow(() -> postService.deletePost(1L));
        verify(postRepository).deleteById(1L);
    }

    @Test
    public void testDeletePost_NotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> postService.deletePost(1L));
    }

    @Test
    public void testListPosts() {
        List<Post> posts = Arrays.asList(post, new Post(2L, "T2", "C2", 2L));
        when(postRepository.findAll()).thenReturn(posts);
        List<Post> result = postService.listPosts();
        assertEquals(2, result.size());
    }

    @Test
    public void testCreatePost_NullPost() {
        assertThrows(IllegalArgumentException.class, () -> postService.createPost(null));
    }
}

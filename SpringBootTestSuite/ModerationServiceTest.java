import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ModerationServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private ModerationService moderationService;

    private Post post;
    private Comment comment;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        post = new Post(1L, "Title", "Content", 1L);
        comment = new Comment(1L, 1L, "Comment", 1L);
    }

    @Test
    public void testApprovePost_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        assertDoesNotThrow(() -> moderationService.approvePost(1L));
        verify(postRepository).save(any(Post.class));
    }

    @Test
    public void testApprovePost_NotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> moderationService.approvePost(1L));
    }

    @Test
    public void testRejectPost_Success() {
        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postRepository.save(any(Post.class))).thenReturn(post);
        assertDoesNotThrow(() -> moderationService.rejectPost(1L));
        verify(postRepository).save(any(Post.class));
    }

    @Test
    public void testRejectPost_NotFound() {
        when(postRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> moderationService.rejectPost(1L));
    }

    @Test
    public void testApproveComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        assertDoesNotThrow(() -> moderationService.approveComment(1L));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    public void testApproveComment_NotFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> moderationService.approveComment(1L));
    }

    @Test
    public void testRejectComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        assertDoesNotThrow(() -> moderationService.rejectComment(1L));
        verify(commentRepository).save(any(Comment.class));
    }

    @Test
    public void testRejectComment_NotFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> moderationService.rejectComment(1L));
    }
}

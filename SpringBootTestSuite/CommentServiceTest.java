import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class CommentServiceTest {
    @Mock
    private CommentRepository commentRepository;
    @InjectMocks
    private CommentService commentService;

    private Comment comment;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        comment = new Comment(1L, 1L, "Test comment", 1L);
    }

    @Test
    public void testAddComment_Success() {
        when(commentRepository.save(any(Comment.class))).thenReturn(comment);
        Comment result = commentService.addComment(comment);
        assertEquals(comment, result);
    }

    @Test
    public void testGetCommentById_Found() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        Comment result = commentService.getCommentById(1L);
        assertEquals(comment, result);
    }

    @Test
    public void testGetCommentById_NotFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> commentService.getCommentById(1L));
    }

    @Test
    public void testUpdateComment_Success() {
        Comment updated = new Comment(1L, 1L, "Updated comment", 1L);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        when(commentRepository.save(any(Comment.class))).thenReturn(updated);
        Comment result = commentService.updateComment(1L, updated);
        assertEquals("Updated comment", result.getContent());
    }

    @Test
    public void testUpdateComment_NotFound() {
        Comment updated = new Comment(1L, 1L, "Updated comment", 1L);
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> commentService.updateComment(1L, updated));
    }

    @Test
    public void testDeleteComment_Success() {
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));
        doNothing().when(commentRepository).deleteById(1L);
        assertDoesNotThrow(() -> commentService.deleteComment(1L));
        verify(commentRepository).deleteById(1L);
    }

    @Test
    public void testDeleteComment_NotFound() {
        when(commentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> commentService.deleteComment(1L));
    }

    @Test
    public void testListCommentsByPost() {
        List<Comment> comments = Arrays.asList(comment, new Comment(2L, 1L, "Another", 2L));
        when(commentRepository.findByPostId(1L)).thenReturn(comments);
        List<Comment> result = commentService.listCommentsByPost(1L);
        assertEquals(2, result.size());
    }

    @Test
    public void testAddComment_NullComment() {
        assertThrows(IllegalArgumentException.class, () -> commentService.addComment(null));
    }
}

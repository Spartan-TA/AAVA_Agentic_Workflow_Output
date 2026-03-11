import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.*;

@WebMvcTest(CommentController.class)
public class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private CommentService commentService;
    @Autowired
    private ObjectMapper objectMapper;

    private Comment comment;

    @BeforeEach
    public void setUp() {
        comment = new Comment(1L, 1L, "Test comment", 1L);
    }

    @Test
    public void testAddComment_Success() throws Exception {
        when(commentService.addComment(any(Comment.class))).thenReturn(comment);
        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    @Test
    public void testAddComment_InvalidInput() throws Exception {
        Comment invalid = new Comment(null, null, "", null);
        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetCommentById_Found() throws Exception {
        when(commentService.getCommentById(1L)).thenReturn(comment);
        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Test comment"));
    }

    @Test
    public void testGetCommentById_NotFound() throws Exception {
        when(commentService.getCommentById(1L)).thenThrow(new NoSuchElementException());
        mockMvc.perform(get("/comments/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdateComment_Success() throws Exception {
        Comment updated = new Comment(1L, 1L, "Updated comment", 1L);
        when(commentService.updateComment(eq(1L), any(Comment.class))).thenReturn(updated);
        mockMvc.perform(put("/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").value("Updated comment"));
    }

    @Test
    public void testUpdateComment_NotFound() throws Exception {
        when(commentService.updateComment(eq(1L), any(Comment.class))).thenThrow(new NoSuchElementException());
        mockMvc.perform(put("/comments/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteComment_Success() throws Exception {
        doNothing().when(commentService).deleteComment(1L);
        mockMvc.perform(delete("/comments/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteComment_NotFound() throws Exception {
        doThrow(new NoSuchElementException()).when(commentService).deleteComment(1L);
        mockMvc.perform(delete("/comments/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testListCommentsByPost() throws Exception {
        List<Comment> comments = Arrays.asList(comment, new Comment(2L, 1L, "Another", 2L));
        when(commentService.listCommentsByPost(1L)).thenReturn(comments);
        mockMvc.perform(get("/comments/post/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].content").value("Test comment"));
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        mockMvc.perform(post("/comments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(comment)))
                .andExpect(status().isForbidden());
    }
}

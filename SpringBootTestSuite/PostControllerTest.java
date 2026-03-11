import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.*;

@WebMvcTest(PostController.class)
public class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private PostService postService;
    @Autowired
    private ObjectMapper objectMapper;

    private Post post;

    @BeforeEach
    public void setUp() {
        post = new Post(1L, "Title", "Content", 1L);
    }

    @Test
    public void testCreatePost_Success() throws Exception {
        when(postService.createPost(any(Post.class))).thenReturn(post);
        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    public void testCreatePost_InvalidInput() throws Exception {
        Post invalid = new Post(null, "", "", null);
        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetPostById_Found() throws Exception {
        when(postService.getPostById(1L)).thenReturn(post);
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Title"));
    }

    @Test
    public void testGetPostById_NotFound() throws Exception {
        when(postService.getPostById(1L)).thenThrow(new NoSuchElementException());
        mockMvc.perform(get("/posts/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testUpdatePost_Success() throws Exception {
        Post updated = new Post(1L, "NewTitle", "NewContent", 1L);
        when(postService.updatePost(eq(1L), any(Post.class))).thenReturn(updated);
        mockMvc.perform(put("/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("NewTitle"));
    }

    @Test
    public void testUpdatePost_NotFound() throws Exception {
        when(postService.updatePost(eq(1L), any(Post.class))).thenThrow(new NoSuchElementException());
        mockMvc.perform(put("/posts/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeletePost_Success() throws Exception {
        doNothing().when(postService).deletePost(1L);
        mockMvc.perform(delete("/posts/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeletePost_NotFound() throws Exception {
        doThrow(new NoSuchElementException()).when(postService).deletePost(1L);
        mockMvc.perform(delete("/posts/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testListPosts() throws Exception {
        List<Post> posts = Arrays.asList(post, new Post(2L, "T2", "C2", 2L));
        when(postService.listPosts()).thenReturn(posts);
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Title"));
    }

    @Test
    public void testUnauthorizedAccess() throws Exception {
        // Simulate forbidden for unauthorized user
        mockMvc.perform(post("/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(post)))
                .andExpect(status().isForbidden());
    }
}

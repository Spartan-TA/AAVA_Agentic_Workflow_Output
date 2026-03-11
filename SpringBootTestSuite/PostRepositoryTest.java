import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
public class PostRepositoryTest {
    @Autowired
    private PostRepository postRepository;

    private Post post1;
    private Post post2;

    @BeforeEach
    public void setUp() {
        postRepository.deleteAll();
        post1 = new Post(null, "Title1", "Content1", 1L);
        post2 = new Post(null, "Title2", "Content2", 2L);
        postRepository.save(post1);
        postRepository.save(post2);
    }

    @Test
    public void testFindById_Found() {
        Optional<Post> found = postRepository.findById(post1.getId());
        assertTrue(found.isPresent());
        assertEquals("Title1", found.get().getTitle());
    }

    @Test
    public void testFindById_NotFound() {
        Optional<Post> found = postRepository.findById(999L);
        assertFalse(found.isPresent());
    }

    @Test
    public void testSaveAndDelete() {
        Post post = new Post(null, "Title3", "Content3", 3L);
        Post saved = postRepository.save(post);
        assertNotNull(saved.getId());
        postRepository.delete(saved);
        assertFalse(postRepository.findById(saved.getId()).isPresent());
    }

    @Test
    public void testFindAll() {
        List<Post> posts = postRepository.findAll();
        assertEquals(2, posts.size());
    }

    @Test
    public void testDeleteById() {
        postRepository.deleteById(post1.getId());
        assertFalse(postRepository.findById(post1.getId()).isPresent());
    }

    @Test
    public void testSave_NullFields() {
        Post post = new Post(null, null, null, null);
        assertThrows(Exception.class, () -> postRepository.save(post));
    }
}

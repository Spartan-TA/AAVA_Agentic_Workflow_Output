package SpringBootTestSuite;

import com.example.app.entity.Content;
import com.example.app.repository.ContentRepository;
import com.example.app.service.SearchService;
import org.junit.jupiter.api.*;
import org.mockito.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SearchServiceTest {
    @Mock
    private ContentRepository contentRepository;

    @InjectMocks
    private SearchService searchService;

    private List<Content> contents;

    @BeforeEach
    void setUp() {
        Content c1 = new Content();
        c1.setId(1L);
        c1.setTitle("Spring Boot Guide");
        Content c2 = new Content();
        c2.setId(2L);
        c2.setTitle("JUnit Testing");
        contents = Arrays.asList(c1, c2);
    }

    @Test
    void testSearchWithPartialMatch() {
        when(contentRepository.findByTitleContainingIgnoreCase("Spring")).thenReturn(Collections.singletonList(contents.get(0)));
        List<Content> result = searchService.search("Spring");
        assertEquals(1, result.size());
        assertEquals("Spring Boot Guide", result.get(0).getTitle());
    }

    @Test
    void testSearchWithEmptyResult() {
        when(contentRepository.findByTitleContainingIgnoreCase("Unknown")).thenReturn(Collections.emptyList());
        List<Content> result = searchService.search("Unknown");
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchWithNullInput() {
        assertThrows(NullPointerException.class, () -> searchService.search(null));
    }

    @Test
    void testSearchWithEmptyString() {
        assertThrows(IllegalArgumentException.class, () -> searchService.search(""));
    }

    @Test
    void testSearchWithInvalidFormat() {
        // Assuming invalid format means special characters or too long
        String invalidQuery = "@#$%^&*()";
        when(contentRepository.findByTitleContainingIgnoreCase(invalidQuery)).thenReturn(Collections.emptyList());
        List<Content> result = searchService.search(invalidQuery);
        assertTrue(result.isEmpty());
    }

    @AfterEach
    void tearDown() {
        contents = null;
    }
}

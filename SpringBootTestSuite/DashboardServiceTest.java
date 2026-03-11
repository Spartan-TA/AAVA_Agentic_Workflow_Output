import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class DashboardServiceTest {
    @Mock
    private PostRepository postRepository;
    @Mock
    private CommentRepository commentRepository;
    @Mock
    private LikeRepository likeRepository;
    @InjectMocks
    private DashboardService dashboardService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testGetUserDashboardSummary_Success() {
        when(postRepository.countByUserId(1L)).thenReturn(5L);
        when(commentRepository.countByUserId(1L)).thenReturn(10L);
        when(likeRepository.countByUserId(1L)).thenReturn(20L);
        DashboardSummary summary = dashboardService.getUserDashboardSummary(1L);
        assertEquals(5L, summary.getPostCount());
        assertEquals(10L, summary.getCommentCount());
        assertEquals(20L, summary.getLikeCount());
    }

    @Test
    public void testGetUserDashboardSummary_NoActivity() {
        when(postRepository.countByUserId(2L)).thenReturn(0L);
        when(commentRepository.countByUserId(2L)).thenReturn(0L);
        when(likeRepository.countByUserId(2L)).thenReturn(0L);
        DashboardSummary summary = dashboardService.getUserDashboardSummary(2L);
        assertEquals(0L, summary.getPostCount());
        assertEquals(0L, summary.getCommentCount());
        assertEquals(0L, summary.getLikeCount());
    }

    @Test
    public void testGetUserDashboardSummary_NullUserId() {
        assertThrows(IllegalArgumentException.class, () -> dashboardService.getUserDashboardSummary(null));
    }
}

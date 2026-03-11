import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class LikeServiceTest {
    @Mock
    private LikeRepository likeRepository;
    @InjectMocks
    private LikeService likeService;

    private Like like;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        like = new Like(1L, 1L, 1L);
    }

    @Test
    public void testAddLike_Success() {
        when(likeRepository.save(any(Like.class))).thenReturn(like);
        Like result = likeService.addLike(like);
        assertEquals(like, result);
    }

    @Test
    public void testAddLike_Duplicate() {
        when(likeRepository.existsByUserIdAndPostId(1L, 1L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> likeService.addLike(like));
    }

    @Test
    public void testRemoveLike_Success() {
        when(likeRepository.findByUserIdAndPostId(1L, 1L)).thenReturn(Optional.of(like));
        doNothing().when(likeRepository).delete(like);
        assertDoesNotThrow(() -> likeService.removeLike(1L, 1L));
        verify(likeRepository).delete(like);
    }

    @Test
    public void testRemoveLike_NotFound() {
        when(likeRepository.findByUserIdAndPostId(1L, 1L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> likeService.removeLike(1L, 1L));
    }

    @Test
    public void testCountLikesForPost() {
        when(likeRepository.countByPostId(1L)).thenReturn(5L);
        long count = likeService.countLikesForPost(1L);
        assertEquals(5L, count);
    }

    @Test
    public void testAddLike_NullLike() {
        assertThrows(IllegalArgumentException.class, () -> likeService.addLike(null));
    }
}

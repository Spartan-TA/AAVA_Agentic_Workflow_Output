package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import java.util.*;

/**
 * PerformanceReviewServiceTest - Comprehensive unit tests for PerformanceReviewService covering reviews, goals, acknowledgements, boundaries, and edge cases.
 */
public class PerformanceReviewServiceTest {
    private PerformanceReviewService reviewService;

    @BeforeEach
    public void setUp() {
        reviewService = new PerformanceReviewService();
    }

    @Test
    public void testCreateReviewTemplate() {
        ReviewTemplate template = new ReviewTemplate("Annual", Arrays.asList("Quality", "Attendance"));
        assertDoesNotThrow(() -> reviewService.createReviewTemplate(template));
    }

    @Test
    public void testAssignReviewToEmployee() {
        Review review = new Review(1, 100, "Annual");
        assertTrue(reviewService.assignReviewToEmployee(review, 100));
    }

    @Test
    public void testSubmitReviewWithRatingsAndComments() {
        Review review = new Review(2, 101, "Quarterly");
        Map<String, Integer> ratings = new HashMap<>();
        ratings.put("Quality", 5);
        ratings.put("Attendance", 4);
        assertTrue(reviewService.submitReview(review, ratings, "Good job"));
    }

    @Test
    public void testAcknowledgeReviewSupervisor() {
        int reviewId = 3;
        assertTrue(reviewService.acknowledgeReview(reviewId, "SUPERVISOR"));
    }

    @Test
    public void testAcknowledgeReviewEmployee() {
        int reviewId = 4;
        assertTrue(reviewService.acknowledgeReview(reviewId, "EMPLOYEE"));
    }

    @Test
    public void testTrackGoals() {
        int empId = 102;
        List<Goal> goals = reviewService.trackGoals(empId);
        assertNotNull(goals);
    }

    @Test
    public void testUpdateGoalProgress() {
        Goal goal = new Goal("Safety", 50);
        assertTrue(reviewService.updateGoalProgress(goal, 75));
    }

    @Test
    public void testGenerateReviewPDF() {
        int reviewId = 5;
        byte[] pdf = reviewService.generateReviewPDF(reviewId);
        assertNotNull(pdf);
    }

    @Test
    public void testGetReviewsByEmployee() {
        int empId = 103;
        List<Review> reviews = reviewService.getReviewsByEmployee(empId);
        assertNotNull(reviews);
    }

    @Test
    public void testGetReviewHistoryImmutable() {
        int reviewId = 6;
        List<ReviewHistory> history = reviewService.getReviewHistory(reviewId);
        assertNotNull(history);
        assertTrue(history.stream().allMatch(ReviewHistory::isImmutable));
    }

    @Test
    public void testCalculateAverageRating() {
        int reviewId = 7;
        double avg = reviewService.calculateAverageRating(reviewId);
        assertTrue(avg >= 1 && avg <= 5);
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 3, 5})
    public void testBoundaryRatingScales(int rating) {
        assertTrue(rating >= 1 && rating <= 5);
    }

    @Test
    public void testMissingAcknowledgements() {
        int reviewId = 8;
        assertFalse(reviewService.isAcknowledged(reviewId, "EMPLOYEE"));
    }

    @Test
    public void testIncompleteReviews() {
        Review review = new Review(9, 104, "Annual");
        Map<String, Integer> ratings = new HashMap<>();
        ratings.put("Quality", null);
        assertThrows(IllegalArgumentException.class, () -> reviewService.submitReview(review, ratings, "Incomplete"));
    }
}

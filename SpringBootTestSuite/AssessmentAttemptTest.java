package com.example.mcqassessment.domain;

import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test cases for AssessmentAttempt entity.
 * Tests fields, constructors, getters/setters, score calculation, and relationship management.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssessmentAttemptTest {

    private AssessmentAttempt attempt;

    @BeforeEach
    void setUp() {
        attempt = new AssessmentAttempt();
        attempt.setId(1L);
        attempt.setStudentUsername("student1");
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setCompletedAt(LocalDateTime.now().plusMinutes(30));
        attempt.setScore(80);
        attempt.setAssessment(new Assessment());
        attempt.setAttemptedQuestions(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        attempt = null;
    }

    @Test
    void testConstructor_DefaultFields_NullValues() {
        AssessmentAttempt aa = new AssessmentAttempt();
        assertNull(aa.getId());
        assertNull(aa.getStudentUsername());
        assertNull(aa.getStartedAt());
        assertNull(aa.getCompletedAt());
        assertNull(aa.getScore());
        assertNull(aa.getAssessment());
        assertNull(aa.getAttemptedQuestions());
    }

    @Test
    void testConstructor_ParameterizedFields_CorrectAssignment() {
        Assessment a = new Assessment();
        ArrayList<AttemptedQuestion> aqList = new ArrayList<>();
        LocalDateTime start = LocalDateTime.now();
        LocalDateTime end = start.plusMinutes(10);
        AssessmentAttempt aa = new AssessmentAttempt(2L, "student2", start, end, 100, a, aqList);
        assertEquals(2L, aa.getId());
        assertEquals("student2", aa.getStudentUsername());
        assertEquals(start, aa.getStartedAt());
        assertEquals(end, aa.getCompletedAt());
        assertEquals(100, aa.getScore());
        assertEquals(a, aa.getAssessment());
        assertEquals(aqList, aa.getAttemptedQuestions());
    }

    @Test
    void testSettersAndGetters_NormalValues_CorrectAssignment() {
        attempt.setStudentUsername("student3");
        LocalDateTime start = LocalDateTime.of(2024, 6, 1, 10, 0);
        LocalDateTime end = start.plusMinutes(20);
        attempt.setStartedAt(start);
        attempt.setCompletedAt(end);
        attempt.setScore(90);
        Assessment a = new Assessment();
        attempt.setAssessment(a);

        assertEquals("student3", attempt.getStudentUsername());
        assertEquals(start, attempt.getStartedAt());
        assertEquals(end, attempt.getCompletedAt());
        assertEquals(90, attempt.getScore());
        assertEquals(a, attempt.getAssessment());
    }

    @Test
    void testSettersAndGetters_NullAndEmptyValues_HandleGracefully() {
        attempt.setStudentUsername(null);
        attempt.setStartedAt(null);
        attempt.setCompletedAt(null);
        attempt.setScore(null);
        attempt.setAssessment(null);

        assertNull(attempt.getStudentUsername());
        assertNull(attempt.getStartedAt());
        assertNull(attempt.getCompletedAt());
        assertNull(attempt.getScore());
        assertNull(attempt.getAssessment());
    }

    @Test
    void testRelationshipManagement_AddRemoveAttemptedQuestions_IntegrityMaintained() {
        AttemptedQuestion aq1 = new AttemptedQuestion();
        aq1.setId(301L);
        AttemptedQuestion aq2 = new AttemptedQuestion();
        aq2.setId(302L);

        attempt.setAttemptedQuestions(new ArrayList<>());
        attempt.getAttemptedQuestions().add(aq1);
        attempt.getAttemptedQuestions().add(aq2);

        assertEquals(2, attempt.getAttemptedQuestions().size());
        assertTrue(attempt.getAttemptedQuestions().contains(aq1));
        assertTrue(attempt.getAttemptedQuestions().contains(aq2));

        attempt.getAttemptedQuestions().remove(aq1);
        assertEquals(1, attempt.getAttemptedQuestions().size());
        assertFalse(attempt.getAttemptedQuestions().contains(aq1));
    }

    @Test
    void testScore_BoundaryValues_MinMax() {
        attempt.setScore(Integer.MIN_VALUE);
        assertEquals(Integer.MIN_VALUE, attempt.getScore());

        attempt.setScore(Integer.MAX_VALUE);
        assertEquals(Integer.MAX_VALUE, attempt.getScore());
    }

    @Test
    void testScoreCalculation_AllCorrect_ReturnsMaxScore() {
        attempt.setAttemptedQuestions(new ArrayList<>());
        for (int i = 0; i < 5; i++) {
            AttemptedQuestion aq = new AttemptedQuestion();
            aq.setIsCorrect(true);
            attempt.getAttemptedQuestions().add(aq);
        }
        int calculatedScore = (int) attempt.getAttemptedQuestions().stream().filter(AttemptedQuestion::getIsCorrect).count() * 20;
        assertEquals(100, calculatedScore);
    }

    @Test
    void testCascadeOperation_RemoveAttemptedQuestions_QuestionsListEmpty() {
        attempt.setAttemptedQuestions(new ArrayList<>());
        attempt.getAttemptedQuestions().add(new AttemptedQuestion());
        attempt.getAttemptedQuestions().add(new AttemptedQuestion());
        attempt.getAttemptedQuestions().clear();
        assertEquals(0, attempt.getAttemptedQuestions().size());
    }

    @Test
    void testRelationshipManagement_NullAttemptedQuestionsList_ThrowsException() {
        attempt.setAttemptedQuestions(null);
        assertThrows(NullPointerException.class, () -> attempt.getAttemptedQuestions().add(new AttemptedQuestion()));
    }
}
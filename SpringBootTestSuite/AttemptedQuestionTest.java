package com.example.mcqassessment.domain;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test cases for AttemptedQuestion entity.
 * Tests fields, constructors, getters/setters, and relationship management.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttemptedQuestionTest {

    private AttemptedQuestion attemptedQuestion;

    @BeforeEach
    void setUp() {
        attemptedQuestion = new AttemptedQuestion();
        attemptedQuestion.setId(1L);
        attemptedQuestion.setSelectedChoice("A");
        attemptedQuestion.setIsCorrect(true);
        attemptedQuestion.setFeedback("Good job!");
        attemptedQuestion.setQuestion(new Question());
        attemptedQuestion.setAssessmentAttempt(new AssessmentAttempt());
    }

    @AfterEach
    void tearDown() {
        attemptedQuestion = null;
    }

    @Test
    void testConstructor_DefaultFields_NullValues() {
        AttemptedQuestion aq = new AttemptedQuestion();
        assertNull(aq.getId());
        assertNull(aq.getSelectedChoice());
        assertNull(aq.getIsCorrect());
        assertNull(aq.getFeedback());
        assertNull(aq.getQuestion());
        assertNull(aq.getAssessmentAttempt());
    }

    @Test
    void testConstructor_ParameterizedFields_CorrectAssignment() {
        Question q = new Question();
        AssessmentAttempt aa = new AssessmentAttempt();
        AttemptedQuestion aq = new AttemptedQuestion(2L, "B", false, "Needs improvement", q, aa);
        assertEquals(2L, aq.getId());
        assertEquals("B", aq.getSelectedChoice());
        assertFalse(aq.getIsCorrect());
        assertEquals("Needs improvement", aq.getFeedback());
        assertEquals(q, aq.getQuestion());
        assertEquals(aa, aq.getAssessmentAttempt());
    }

    @Test
    void testSettersAndGetters_NormalValues_CorrectAssignment() {
        attemptedQuestion.setSelectedChoice("C");
        attemptedQuestion.setIsCorrect(false);
        attemptedQuestion.setFeedback("Try again");
        Question q = new Question();
        AssessmentAttempt aa = new AssessmentAttempt();
        attemptedQuestion.setQuestion(q);
        attemptedQuestion.setAssessmentAttempt(aa);

        assertEquals("C", attemptedQuestion.getSelectedChoice());
        assertFalse(attemptedQuestion.getIsCorrect());
        assertEquals("Try again", attemptedQuestion.getFeedback());
        assertEquals(q, attemptedQuestion.getQuestion());
        assertEquals(aa, attemptedQuestion.getAssessmentAttempt());
    }

    @Test
    void testSettersAndGetters_NullAndEmptyValues_HandleGracefully() {
        attemptedQuestion.setSelectedChoice(null);
        attemptedQuestion.setIsCorrect(null);
        attemptedQuestion.setFeedback("");
        attemptedQuestion.setQuestion(null);
        attemptedQuestion.setAssessmentAttempt(null);

        assertNull(attemptedQuestion.getSelectedChoice());
        assertNull(attemptedQuestion.getIsCorrect());
        assertEquals("", attemptedQuestion.getFeedback());
        assertNull(attemptedQuestion.getQuestion());
        assertNull(attemptedQuestion.getAssessmentAttempt());
    }

    @Test
    void testId_BoundaryValues_MinMax() {
        attemptedQuestion.setId(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, attemptedQuestion.getId());

        attemptedQuestion.setId(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, attemptedQuestion.getId());
    }

    @Test
    void testSelectedChoice_InvalidFormats_AllowedAsStrings() {
        attemptedQuestion.setSelectedChoice("!@#$%");
        assertEquals("!@#$%", attemptedQuestion.getSelectedChoice());
    }
}
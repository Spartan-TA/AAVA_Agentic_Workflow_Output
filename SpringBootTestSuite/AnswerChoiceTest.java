package com.example.mcqassessment.domain;

import org.junit.jupiter.api.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test cases for AnswerChoice entity.
 * Tests fields, constructors, getters/setters, and relationship management.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnswerChoiceTest {

    private AnswerChoice answerChoice;

    @BeforeEach
    void setUp() {
        answerChoice = new AnswerChoice();
        answerChoice.setId(1L);
        answerChoice.setLabel("A");
        answerChoice.setText("Option A");
        answerChoice.setQuestion(new Question());
    }

    @AfterEach
    void tearDown() {
        answerChoice = null;
    }

    @Test
    void testConstructor_DefaultFields_NullValues() {
        AnswerChoice ac = new AnswerChoice();
        assertNull(ac.getId());
        assertNull(ac.getLabel());
        assertNull(ac.getText());
        assertNull(ac.getQuestion());
    }

    @Test
    void testConstructor_ParameterizedFields_CorrectAssignment() {
        Question q = new Question();
        AnswerChoice ac = new AnswerChoice(2L, "B", "Option B", q);
        assertEquals(2L, ac.getId());
        assertEquals("B", ac.getLabel());
        assertEquals("Option B", ac.getText());
        assertEquals(q, ac.getQuestion());
    }

    @Test
    void testSettersAndGetters_NormalValues_CorrectAssignment() {
        answerChoice.setLabel("C");
        answerChoice.setText("Option C");
        Question q = new Question();
        answerChoice.setQuestion(q);

        assertEquals("C", answerChoice.getLabel());
        assertEquals("Option C", answerChoice.getText());
        assertEquals(q, answerChoice.getQuestion());
    }

    @Test
    void testSettersAndGetters_NullAndEmptyValues_HandleGracefully() {
        answerChoice.setLabel(null);
        answerChoice.setText("");
        answerChoice.setQuestion(null);

        assertNull(answerChoice.getLabel());
        assertEquals("", answerChoice.getText());
        assertNull(answerChoice.getQuestion());
    }

    @Test
    void testId_BoundaryValues_MinMax() {
        answerChoice.setId(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, answerChoice.getId());

        answerChoice.setId(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, answerChoice.getId());
    }

    @Test
    void testLabel_InvalidFormats_AllowedAsStrings() {
        answerChoice.setLabel("!@#$%");
        assertEquals("!@#$%", answerChoice.getLabel());
    }
}
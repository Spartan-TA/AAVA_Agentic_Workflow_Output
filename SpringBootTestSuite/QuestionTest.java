package com.example.mcqassessment.domain;

import org.junit.jupiter.api.*;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test cases for Question entity.
 * Tests fields, constructors, getters/setters, and relationship management.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionTest {

    private Question question;

    @BeforeEach
    void setUp() {
        question = new Question();
        question.setId(1L);
        question.setAssertionText("Assertion");
        question.setReasonText("Reason");
        question.setExplanation("Explanation");
        question.setCorrectChoice("A");
        question.setAssessment(new Assessment());
        question.setAnswerChoices(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        question = null;
    }

    @Test
    void testConstructor_DefaultFields_NullValues() {
        Question q = new Question();
        assertNull(q.getId());
        assertNull(q.getAssertionText());
        assertNull(q.getReasonText());
        assertNull(q.getExplanation());
        assertNull(q.getCorrectChoice());
        assertNull(q.getAssessment());
        assertNull(q.getAnswerChoices());
    }

    @Test
    void testConstructor_ParameterizedFields_CorrectAssignment() {
        Assessment a = new Assessment();
        List<AnswerChoice> choices = new ArrayList<>();
        Question q = new Question(2L, "Assert", "Reason", "Explain", "B", a, choices);
        assertEquals(2L, q.getId());
        assertEquals("Assert", q.getAssertionText());
        assertEquals("Reason", q.getReasonText());
        assertEquals("Explain", q.getExplanation());
        assertEquals("B", q.getCorrectChoice());
        assertEquals(a, q.getAssessment());
        assertEquals(choices, q.getAnswerChoices());
    }

    @Test
    void testSettersAndGetters_NormalValues_CorrectAssignment() {
        question.setAssertionText("New Assertion");
        question.setReasonText("New Reason");
        question.setExplanation("New Explanation");
        question.setCorrectChoice("C");
        Assessment a = new Assessment();
        question.setAssessment(a);

        assertEquals("New Assertion", question.getAssertionText());
        assertEquals("New Reason", question.getReasonText());
        assertEquals("New Explanation", question.getExplanation());
        assertEquals("C", question.getCorrectChoice());
        assertEquals(a, question.getAssessment());
    }

    @Test
    void testSettersAndGetters_NullAndEmptyValues_HandleGracefully() {
        question.setAssertionText(null);
        question.setReasonText("");
        question.setExplanation(null);
        question.setCorrectChoice("");
        question.setAssessment(null);

        assertNull(question.getAssertionText());
        assertEquals("", question.getReasonText());
        assertNull(question.getExplanation());
        assertEquals("", question.getCorrectChoice());
        assertNull(question.getAssessment());
    }

    @Test
    void testRelationshipManagement_AddRemoveAnswerChoices_IntegrityMaintained() {
        AnswerChoice ac1 = new AnswerChoice();
        ac1.setId(201L);
        AnswerChoice ac2 = new AnswerChoice();
        ac2.setId(202L);

        question.setAnswerChoices(new ArrayList<>());
        question.getAnswerChoices().add(ac1);
        question.getAnswerChoices().add(ac2);

        assertEquals(2, question.getAnswerChoices().size());
        assertTrue(question.getAnswerChoices().contains(ac1));
        assertTrue(question.getAnswerChoices().contains(ac2));

        question.getAnswerChoices().remove(ac1);
        assertEquals(1, question.getAnswerChoices().size());
        assertFalse(question.getAnswerChoices().contains(ac1));
    }

    @Test
    void testId_BoundaryValues_MinMax() {
        question.setId(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, question.getId());

        question.setId(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, question.getId());
    }

    @Test
    void testCorrectChoice_InvalidFormats_AllowedAsStrings() {
        question.setCorrectChoice("!@#$%");
        assertEquals("!@#$%", question.getCorrectChoice());
    }

    @Test
    void testCascadeOperation_RemoveAnswerChoices_ChoicesListEmpty() {
        question.setAnswerChoices(new ArrayList<>());
        question.getAnswerChoices().add(new AnswerChoice());
        question.getAnswerChoices().add(new AnswerChoice());
        question.getAnswerChoices().clear();
        assertEquals(0, question.getAnswerChoices().size());
    }

    @Test
    void testRelationshipManagement_NullAnswerChoicesList_ThrowsException() {
        question.setAnswerChoices(null);
        assertThrows(NullPointerException.class, () -> question.getAnswerChoices().add(new AnswerChoice()));
    }
}
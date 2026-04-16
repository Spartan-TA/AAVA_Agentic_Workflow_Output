package com.example.mcqassessment.domain;

import org.junit.jupiter.api.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit 5 test cases for Assessment entity.
 * Covers normal, boundary, and edge cases for fields, constructors, getters/setters, and relationship management.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssessmentTest {

    private Assessment assessment;

    @BeforeEach
    void setUp() {
        assessment = new Assessment();
        assessment.setId(1L);
        assessment.setTitle("MCQ Test");
        assessment.setWeek("Week 1");
        assessment.setTopic("Java Basics");
        assessment.setCreatedBy("admin");
        assessment.setCreatedAt(LocalDateTime.now());
        assessment.setQuestions(new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        assessment = null;
    }

    /**
     * Test default constructor and field initialization.
     */
    @Test
    void testConstructor_DefaultFields_NullValues() {
        Assessment a = new Assessment();
        assertNull(a.getId());
        assertNull(a.getTitle());
        assertNull(a.getWeek());
        assertNull(a.getTopic());
        assertNull(a.getCreatedBy());
        assertNull(a.getCreatedAt());
        assertNull(a.getQuestions());
    }

    /**
     * Test parameterized constructor.
     */
    @Test
    void testConstructor_ParameterizedFields_CorrectAssignment() {
        LocalDateTime now = LocalDateTime.now();
        Assessment a = new Assessment(2L, "Quiz", "Week 2", "OOP", "teacher", now, new ArrayList<>());
        assertEquals(2L, a.getId());
        assertEquals("Quiz", a.getTitle());
        assertEquals("Week 2", a.getWeek());
        assertEquals("OOP", a.getTopic());
        assertEquals("teacher", a.getCreatedBy());
        assertEquals(now, a.getCreatedAt());
        assertNotNull(a.getQuestions());
    }

    /**
     * Test setters and getters for normal values.
     */
    @Test
    void testSettersAndGetters_NormalValues_CorrectAssignment() {
        assessment.setTitle("Updated Title");
        assessment.setWeek("Week 3");
        assessment.setTopic("Spring Boot");
        assessment.setCreatedBy("user1");
        LocalDateTime date = LocalDateTime.of(2024, 6, 1, 10, 0);
        assessment.setCreatedAt(date);

        assertEquals("Updated Title", assessment.getTitle());
        assertEquals("Week 3", assessment.getWeek());
        assertEquals("Spring Boot", assessment.getTopic());
        assertEquals("user1", assessment.getCreatedBy());
        assertEquals(date, assessment.getCreatedAt());
    }

    /**
     * Test setters and getters for null and empty values.
     */
    @Test
    void testSettersAndGetters_NullAndEmptyValues_HandleGracefully() {
        assessment.setTitle(null);
        assessment.setWeek("");
        assessment.setTopic(null);
        assessment.setCreatedBy("");
        assessment.setCreatedAt(null);

        assertNull(assessment.getTitle());
        assertEquals("", assessment.getWeek());
        assertNull(assessment.getTopic());
        assertEquals("", assessment.getCreatedBy());
        assertNull(assessment.getCreatedAt());
    }

    /**
     * Test relationship management: adding and removing questions.
     */
    @Test
    void testRelationshipManagement_AddRemoveQuestions_IntegrityMaintained() {
        Question q1 = new Question();
        q1.setId(101L);
        Question q2 = new Question();
        q2.setId(102L);

        assessment.setQuestions(new ArrayList<>());
        assessment.getQuestions().add(q1);
        assessment.getQuestions().add(q2);

        assertEquals(2, assessment.getQuestions().size());
        assertTrue(assessment.getQuestions().contains(q1));
        assertTrue(assessment.getQuestions().contains(q2));

        assessment.getQuestions().remove(q1);
        assertEquals(1, assessment.getQuestions().size());
        assertFalse(assessment.getQuestions().contains(q1));
    }

    /**
     * Test boundary values for id field.
     */
    @Test
    void testId_BoundaryValues_MinMax() {
        assessment.setId(Long.MIN_VALUE);
        assertEquals(Long.MIN_VALUE, assessment.getId());

        assessment.setId(Long.MAX_VALUE);
        assertEquals(Long.MAX_VALUE, assessment.getId());
    }

    /**
     * Test invalid formats for week and topic.
     */
    @Test
    void testWeekAndTopic_InvalidFormats_AllowedAsStrings() {
        assessment.setWeek("!@#$%");
        assessment.setTopic("123456");
        assertEquals("!@#$%", assessment.getWeek());
        assertEquals("123456", assessment.getTopic());
    }

    /**
     * Test cascade operation simulation (removing questions).
     */
    @Test
    void testCascadeOperation_RemoveQuestions_QuestionsListEmpty() {
        assessment.setQuestions(new ArrayList<>());
        assessment.getQuestions().add(new Question());
        assessment.getQuestions().add(new Question());
        assessment.getQuestions().clear();
        assertEquals(0, assessment.getQuestions().size());
    }

    /**
     * Test exception handling for null questions list.
     */
    @Test
    void testRelationshipManagement_NullQuestionsList_ThrowsException() {
        assessment.setQuestions(null);
        assertThrows(NullPointerException.class, () -> assessment.getQuestions().add(new Question()));
    }
}
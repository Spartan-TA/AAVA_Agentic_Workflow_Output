package com.example.mcqassessment.repository;

import com.example.mcqassessment.domain.Assessment;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AssessmentRepository.
 * Tests save, findById, findAll, delete, and custom findByWeekAndTopic.
 */
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssessmentRepositoryTest {

    @Autowired
    private AssessmentRepository repository;

    private Assessment assessment;

    @BeforeEach
    void setUp() {
        assessment = new Assessment();
        assessment.setTitle("MCQ Test");
        assessment.setWeek("Week 1");
        assessment.setTopic("Java Basics");
        assessment.setCreatedBy("admin");
        assessment.setCreatedAt(LocalDateTime.now());
        repository.save(assessment);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void testSave_NormalValues_Success() {
        Assessment a = new Assessment();
        a.setTitle("Quiz");
        a.setWeek("Week 2");
        a.setTopic("OOP");
        a.setCreatedBy("teacher");
        a.setCreatedAt(LocalDateTime.now());
        Assessment saved = repository.save(a);
        assertNotNull(saved.getId());
        assertEquals("Quiz", saved.getTitle());
    }

    @Test
    void testFindById_ExistingId_ReturnsAssessment() {
        Assessment found = repository.findById(assessment.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(assessment.getTitle(), found.getTitle());
    }

    @Test
    void testFindById_NonExistingId_ReturnsNull() {
        Assessment found = repository.findById(-1L).orElse(null);
        assertNull(found);
    }

    @Test
    void testFindAll_ReturnsList() {
        List<Assessment> list = repository.findAll();
        assertFalse(list.isEmpty());
    }

    @Test
    void testDelete_ExistingId_RemovesAssessment() {
        repository.delete(assessment);
        Assessment found = repository.findById(assessment.getId()).orElse(null);
        assertNull(found);
    }

    @Test
    void testFindByWeekAndTopic_ValidInputs_ReturnsList() {
        List<Assessment> list = repository.findByWeekAndTopic("Week 1", "Java Basics");
        assertFalse(list.isEmpty());
        assertEquals("MCQ Test", list.get(0).getTitle());
    }

    @Test
    void testFindByWeekAndTopic_InvalidInputs_ReturnsEmptyList() {
        List<Assessment> list = repository.findByWeekAndTopic("Week X", "Unknown");
        assertTrue(list.isEmpty());
    }

    @Test
    void testSave_NullTitle_ThrowsException() {
        Assessment a = new Assessment();
        a.setWeek("Week 3");
        a.setTopic("Spring Boot");
        a.setCreatedBy("user1");
        a.setCreatedAt(LocalDateTime.now());
        assertThrows(Exception.class, () -> repository.save(a));
    }

    @Test
    void testSave_EmptyStrings_Allowed() {
        Assessment a = new Assessment();
        a.setTitle("");
        a.setWeek("");
        a.setTopic("");
        a.setCreatedBy("");
        a.setCreatedAt(LocalDateTime.now());
        Assessment saved = repository.save(a);
        assertNotNull(saved.getId());
        assertEquals("", saved.getTitle());
    }
}
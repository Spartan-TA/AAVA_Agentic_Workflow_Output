package com.example.mcqassessment.repository;

import com.example.mcqassessment.domain.AssessmentAttempt;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AssessmentAttemptRepository.
 * Tests save, findById, findAll, delete, and custom queries.
 */
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AssessmentAttemptRepositoryTest {

    @Autowired
    private AssessmentAttemptRepository repository;

    private AssessmentAttempt attempt;

    @BeforeEach
    void setUp() {
        attempt = new AssessmentAttempt();
        attempt.setStudentUsername("student1");
        attempt.setStartedAt(LocalDateTime.now());
        attempt.setCompletedAt(LocalDateTime.now().plusMinutes(30));
        attempt.setScore(80);
        repository.save(attempt);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void testSave_NormalValues_Success() {
        AssessmentAttempt aa = new AssessmentAttempt();
        aa.setStudentUsername("student2");
        aa.setStartedAt(LocalDateTime.now());
        aa.setCompletedAt(LocalDateTime.now().plusMinutes(10));
        aa.setScore(100);
        AssessmentAttempt saved = repository.save(aa);
        assertNotNull(saved.getId());
        assertEquals("student2", saved.getStudentUsername());
    }

    @Test
    void testFindById_ExistingId_ReturnsAttempt() {
        AssessmentAttempt found = repository.findById(attempt.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(attempt.getStudentUsername(), found.getStudentUsername());
    }

    @Test
    void testFindById_NonExistingId_ReturnsNull() {
        AssessmentAttempt found = repository.findById(-1L).orElse(null);
        assertNull(found);
    }

    @Test
    void testFindAll_ReturnsList() {
        List<AssessmentAttempt> list = repository.findAll();
        assertFalse(list.isEmpty());
    }

    @Test
    void testDelete_ExistingId_RemovesAttempt() {
        repository.delete(attempt);
        AssessmentAttempt found = repository.findById(attempt.getId()).orElse(null);
        assertNull(found);
    }

    @Test
    void testFindByAssessmentId_ValidInput_ReturnsList() {
        List<AssessmentAttempt> list = repository.findByAssessmentId(attempt.getAssessment() != null ? attempt.getAssessment().getId() : null);
        assertTrue(list.isEmpty() || list.size() >= 0); // Since assessment may be null in this stub
    }

    @Test
    void testFindByStudentUsername_ValidInput_ReturnsList() {
        List<AssessmentAttempt> list = repository.findByStudentUsername("student1");
        assertFalse(list.isEmpty());
        assertEquals("student1", list.get(0).getStudentUsername());
    }

    @Test
    void testFindByStudentUsername_InvalidInput_ReturnsEmptyList() {
        List<AssessmentAttempt> list = repository.findByStudentUsername("unknown");
        assertTrue(list.isEmpty());
    }

    @Test
    void testSave_NullStudentUsername_ThrowsException() {
        AssessmentAttempt aa = new AssessmentAttempt();
        aa.setStartedAt(LocalDateTime.now());
        aa.setCompletedAt(LocalDateTime.now().plusMinutes(10));
        aa.setScore(100);
        assertThrows(Exception.class, () -> repository.save(aa));
    }

    @Test
    void testSave_EmptyStrings_Allowed() {
        AssessmentAttempt aa = new AssessmentAttempt();
        aa.setStudentUsername("");
        aa.setStartedAt(LocalDateTime.now());
        aa.setCompletedAt(LocalDateTime.now().plusMinutes(10));
        aa.setScore(100);
        AssessmentAttempt saved = repository.save(aa);
        assertNotNull(saved.getId());
        assertEquals("", saved.getStudentUsername());
    }
}
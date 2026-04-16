package com.example.mcqassessment.repository;

import com.example.mcqassessment.domain.Question;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for QuestionRepository.
 * Tests save, findById, findAll, delete.
 */
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class QuestionRepositoryTest {

    @Autowired
    private QuestionRepository repository;

    private Question question;

    @BeforeEach
    void setUp() {
        question = new Question();
        question.setAssertionText("Assertion");
        question.setReasonText("Reason");
        question.setExplanation("Explanation");
        question.setCorrectChoice("A");
        repository.save(question);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void testSave_NormalValues_Success() {
        Question q = new Question();
        q.setAssertionText("Assert");
        q.setReasonText("Reason");
        q.setExplanation("Explain");
        q.setCorrectChoice("B");
        Question saved = repository.save(q);
        assertNotNull(saved.getId());
        assertEquals("Assert", saved.getAssertionText());
    }

    @Test
    void testFindById_ExistingId_ReturnsQuestion() {
        Question found = repository.findById(question.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(question.getAssertionText(), found.getAssertionText());
    }

    @Test
    void testFindById_NonExistingId_ReturnsNull() {
        Question found = repository.findById(-1L).orElse(null);
        assertNull(found);
    }

    @Test
    void testFindAll_ReturnsList() {
        List<Question> list = repository.findAll();
        assertFalse(list.isEmpty());
    }

    @Test
    void testDelete_ExistingId_RemovesQuestion() {
        repository.delete(question);
        Question found = repository.findById(question.getId()).orElse(null);
        assertNull(found);
    }

    @Test
    void testSave_NullAssertionText_ThrowsException() {
        Question q = new Question();
        q.setReasonText("Reason");
        q.setExplanation("Explanation");
        q.setCorrectChoice("A");
        assertThrows(Exception.class, () -> repository.save(q));
    }

    @Test
    void testSave_EmptyStrings_Allowed() {
        Question q = new Question();
        q.setAssertionText("");
        q.setReasonText("");
        q.setExplanation("");
        q.setCorrectChoice("");
        Question saved = repository.save(q);
        assertNotNull(saved.getId());
        assertEquals("", saved.getAssertionText());
    }
}
package com.example.mcqassessment.repository;

import com.example.mcqassessment.domain.AttemptedQuestion;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AttemptedQuestionRepository.
 * Tests save, findById, findAll, delete.
 */
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AttemptedQuestionRepositoryTest {

    @Autowired
    private AttemptedQuestionRepository repository;

    private AttemptedQuestion attemptedQuestion;

    @BeforeEach
    void setUp() {
        attemptedQuestion = new AttemptedQuestion();
        attemptedQuestion.setSelectedChoice("A");
        attemptedQuestion.setIsCorrect(true);
        attemptedQuestion.setFeedback("Good job!");
        repository.save(attemptedQuestion);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void testSave_NormalValues_Success() {
        AttemptedQuestion aq = new AttemptedQuestion();
        aq.setSelectedChoice("B");
        aq.setIsCorrect(false);
        aq.setFeedback("Needs improvement");
        AttemptedQuestion saved = repository.save(aq);
        assertNotNull(saved.getId());
        assertEquals("B", saved.getSelectedChoice());
    }

    @Test
    void testFindById_ExistingId_ReturnsAttemptedQuestion() {
        AttemptedQuestion found = repository.findById(attemptedQuestion.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(attemptedQuestion.getSelectedChoice(), found.getSelectedChoice());
    }

    @Test
    void testFindById_NonExistingId_ReturnsNull() {
        AttemptedQuestion found = repository.findById(-1L).orElse(null);
        assertNull(found);
    }

    @Test
    void testFindAll_ReturnsList() {
        List<AttemptedQuestion> list = repository.findAll();
        assertFalse(list.isEmpty());
    }

    @Test
    void testDelete_ExistingId_RemovesAttemptedQuestion() {
        repository.delete(attemptedQuestion);
        AttemptedQuestion found = repository.findById(attemptedQuestion.getId()).orElse(null);
        assertNull(found);
    }

    @Test
    void testSave_NullSelectedChoice_ThrowsException() {
        AttemptedQuestion aq = new AttemptedQuestion();
        aq.setIsCorrect(true);
        aq.setFeedback("Feedback");
        assertThrows(Exception.class, () -> repository.save(aq));
    }

    @Test
    void testSave_EmptyStrings_Allowed() {
        AttemptedQuestion aq = new AttemptedQuestion();
        aq.setSelectedChoice("");
        aq.setIsCorrect(false);
        aq.setFeedback("");
        AttemptedQuestion saved = repository.save(aq);
        assertNotNull(saved.getId());
        assertEquals("", saved.getSelectedChoice());
    }
}
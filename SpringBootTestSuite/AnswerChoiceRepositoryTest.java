package com.example.mcqassessment.repository;

import com.example.mcqassessment.domain.AnswerChoice;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for AnswerChoiceRepository.
 * Tests save, findById, findAll, delete.
 */
@DataJpaTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class AnswerChoiceRepositoryTest {

    @Autowired
    private AnswerChoiceRepository repository;

    private AnswerChoice answerChoice;

    @BeforeEach
    void setUp() {
        answerChoice = new AnswerChoice();
        answerChoice.setLabel("A");
        answerChoice.setText("Option A");
        repository.save(answerChoice);
    }

    @AfterEach
    void tearDown() {
        repository.deleteAll();
    }

    @Test
    void testSave_NormalValues_Success() {
        AnswerChoice ac = new AnswerChoice();
        ac.setLabel("B");
        ac.setText("Option B");
        AnswerChoice saved = repository.save(ac);
        assertNotNull(saved.getId());
        assertEquals("B", saved.getLabel());
    }

    @Test
    void testFindById_ExistingId_ReturnsAnswerChoice() {
        AnswerChoice found = repository.findById(answerChoice.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(answerChoice.getLabel(), found.getLabel());
    }

    @Test
    void testFindById_NonExistingId_ReturnsNull() {
        AnswerChoice found = repository.findById(-1L).orElse(null);
        assertNull(found);
    }

    @Test
    void testFindAll_ReturnsList() {
        List<AnswerChoice> list = repository.findAll();
        assertFalse(list.isEmpty());
    }

    @Test
    void testDelete_ExistingId_RemovesAnswerChoice() {
        repository.delete(answerChoice);
        AnswerChoice found = repository.findById(answerChoice.getId()).orElse(null);
        assertNull(found);
    }

    @Test
    void testSave_NullLabel_ThrowsException() {
        AnswerChoice ac = new AnswerChoice();
        ac.setText("Option C");
        assertThrows(Exception.class, () -> repository.save(ac));
    }

    @Test
    void testSave_EmptyStrings_Allowed() {
        AnswerChoice ac = new AnswerChoice();
        ac.setLabel("");
        ac.setText("");
        AnswerChoice saved = repository.save(ac);
        assertNotNull(saved.getId());
        assertEquals("", saved.getLabel());
    }
}
package com.warehouse.management.repository;

import com.warehouse.management.entity.Shift;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive repository tests for ShiftRepository
 * Tests conflict detection queries and edge cases
 */
@DataJpaTest
@ActiveProfiles("test")
public class ShiftRepositoryTest {

    @Autowired
    private ShiftRepository shiftRepository;

    @BeforeEach
    void setUp() {
        shiftRepository.deleteAll();
        Shift shift1 = new Shift("Morning", LocalTime.of(8, 0), LocalTime.of(16, 0));
        Shift shift2 = new Shift("Evening", LocalTime.of(16, 0), LocalTime.of(0, 0));
        shiftRepository.save(shift1);
        shiftRepository.save(shift2);
    }

    @Test
    @DisplayName("Test findByName returns correct shift")
    void testFindByName_ReturnsShift() {
        List<Shift> result = shiftRepository.findByName("Morning");
        assertEquals(1, result.size());
        assertEquals("Morning", result.get(0).getName());
    }

    @Test
    @DisplayName("Test findByName with non-existent name returns empty list")
    void testFindByName_NonExistent_ReturnsEmpty() {
        List<Shift> result = shiftRepository.findByName("Night");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test findConflictingShifts returns correct shifts")
    void testFindConflictingShifts_ReturnsShifts() {
        List<Shift> result = shiftRepository.findConflictingShifts(LocalTime.of(15, 0), LocalTime.of(17, 0));
        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Test findConflictingShifts with no conflicts returns empty list")
    void testFindConflictingShifts_NoConflicts_ReturnsEmpty() {
        List<Shift> result = shiftRepository.findConflictingShifts(LocalTime.of(1, 0), LocalTime.of(7, 0));
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test save and retrieve shift")
    void testSaveAndRetrieveShift() {
        Shift shift = new Shift("Night", LocalTime.of(0, 0), LocalTime.of(8, 0));
        shiftRepository.save(shift);
        List<Shift> result = shiftRepository.findByName("Night");
        assertEquals(1, result.size());
        assertEquals(LocalTime.of(0, 0), result.get(0).getStartTime());
    }

    @Test
    @DisplayName("Test delete shift")
    void testDeleteShift() {
        List<Shift> shifts = shiftRepository.findByName("Morning");
        assertFalse(shifts.isEmpty());
        shiftRepository.delete(shifts.get(0));
        List<Shift> result = shiftRepository.findByName("Morning");
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("Test findByName with null input throws exception")
    void testFindByName_NullInput_ThrowsException() {
        assertThrows(Exception.class, () -> shiftRepository.findByName(null));
    }

    @Test
    @DisplayName("Test save shift with null name throws exception")
    void testSaveShift_NullName_ThrowsException() {
        Shift shift = new Shift(null, LocalTime.of(8, 0), LocalTime.of(16, 0));
        assertThrows(Exception.class, () -> shiftRepository.save(shift));
    }
}

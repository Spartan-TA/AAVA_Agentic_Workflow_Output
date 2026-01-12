package com.warehouse.employee;

import com.warehouse.employee.model.ShiftTemplate;
import com.warehouse.employee.repository.ShiftRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalTime;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ShiftRepositoryTest {
    @Autowired
    private ShiftRepository shiftRepository;

    private ShiftTemplate template1, template2, template3;

    @BeforeEach
    void setUp() {
        template1 = new ShiftTemplate(null, "Morning", LocalTime.of(8,0), LocalTime.of(16,0), "WEEKLY", true, "2024-12-25", 1L, null, null);
        template2 = new ShiftTemplate(null, "Evening", LocalTime.of(16,0), LocalTime.of(0,0), "MONTHLY", false, "2024-01-01", 2L, null, null);
        template3 = new ShiftTemplate(null, "Night", LocalTime.of(0,0), LocalTime.of(8,0), "DAILY", true, "", 1L, null, null);
        shiftRepository.save(template1);
        shiftRepository.save(template2);
        shiftRepository.save(template3);
    }

    @Test
    void testFindByNameContainingIgnoreCase() {
        List<ShiftTemplate> found = shiftRepository.findByNameContainingIgnoreCase("morning");
        assertEquals(1, found.size());
        assertEquals("Morning", found.get(0).getName());
        found = shiftRepository.findByNameContainingIgnoreCase("NIGHT");
        assertEquals(1, found.size());
        assertEquals("Night", found.get(0).getName());
        found = shiftRepository.findByNameContainingIgnoreCase("shift");
        assertTrue(found.isEmpty());
    }

    @Test
    void testFindByOvertimeAllowedTrue() {
        List<ShiftTemplate> found = shiftRepository.findByOvertimeAllowedTrue();
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(t -> t.getName().equals("Morning")));
        assertTrue(found.stream().anyMatch(t -> t.getName().equals("Night")));
    }

    @Test
    void testFindByTenantId() {
        List<ShiftTemplate> found = shiftRepository.findByTenantId(1L);
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(t -> t.getName().equals("Morning")));
        assertTrue(found.stream().anyMatch(t -> t.getName().equals("Night")));
        found = shiftRepository.findByTenantId(2L);
        assertEquals(1, found.size());
        assertEquals("Evening", found.get(0).getName());
    }

    @Test
    void testSaveAndFindAll() {
        List<ShiftTemplate> all = shiftRepository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    void testDelete() {
        shiftRepository.delete(template1);
        List<ShiftTemplate> all = shiftRepository.findAll();
        assertEquals(2, all.size());
        assertFalse(all.stream().anyMatch(t -> t.getName().equals("Morning")));
    }

    @Test
    void testEdgeCases() {
        ShiftTemplate emptyName = new ShiftTemplate(null, "", LocalTime.of(10,0), LocalTime.of(18,0), null, null, null, null, null, null);
        shiftRepository.save(emptyName);
        List<ShiftTemplate> found = shiftRepository.findByNameContainingIgnoreCase("");
        assertTrue(found.size() >= 1);
    }
}

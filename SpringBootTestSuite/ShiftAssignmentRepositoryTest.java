package com.warehouse.employee;

import com.warehouse.employee.model.Employee;
import com.warehouse.employee.model.ShiftAssignment;
import com.warehouse.employee.model.ShiftTemplate;
import com.warehouse.employee.repository.ShiftAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ShiftAssignmentRepositoryTest {
    @Autowired
    private ShiftAssignmentRepository shiftAssignmentRepository;

    private Employee emp1, emp2;
    private ShiftTemplate template1, template2;
    private ShiftAssignment assign1, assign2, assign3;

    @BeforeEach
    void setUp() {
        emp1 = new Employee(); emp1.setId(1L); emp1.setFirstName("John");
        emp2 = new Employee(); emp2.setId(2L); emp2.setFirstName("Jane");
        template1 = new ShiftTemplate(); template1.setId(1L); template1.setName("Morning");
        template2 = new ShiftTemplate(); template2.setId(2L); template2.setName("Night");
        assign1 = new ShiftAssignment(null, emp1, template1, LocalDate.of(2024,6,1), false, "ASSIGNED", null, null);
        assign2 = new ShiftAssignment(null, emp1, template2, LocalDate.of(2024,6,2), true, "COMPLETED", null, null);
        assign3 = new ShiftAssignment(null, emp2, template1, LocalDate.of(2024,6,1), false, "CANCELLED", null, null);
        shiftAssignmentRepository.save(assign1);
        shiftAssignmentRepository.save(assign2);
        shiftAssignmentRepository.save(assign3);
    }

    @Test
    void testFindByEmployeeId() {
        List<ShiftAssignment> found = shiftAssignmentRepository.findByEmployeeId(1L);
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(a -> "ASSIGNED".equals(a.getStatus())));
        assertTrue(found.stream().anyMatch(a -> "COMPLETED".equals(a.getStatus())));
    }

    @Test
    void testFindByEmployeeIdAndDateRange() {
        List<ShiftAssignment> found = shiftAssignmentRepository.findByEmployeeIdAndDateRange(1L, LocalDate.of(2024,6,1), LocalDate.of(2024,6,2));
        assertEquals(2, found.size());
        found = shiftAssignmentRepository.findByEmployeeIdAndDateRange(2L, LocalDate.of(2024,6,1), LocalDate.of(2024,6,1));
        assertEquals(1, found.size());
    }

    @Test
    void testFindByShiftDate() {
        List<ShiftAssignment> found = shiftAssignmentRepository.findByShiftDate(LocalDate.of(2024,6,1));
        assertEquals(2, found.size());
        found = shiftAssignmentRepository.findByShiftDate(LocalDate.of(2024,6,2));
        assertEquals(1, found.size());
    }

    @Test
    void testFindByIsOvertimeTrue() {
        List<ShiftAssignment> found = shiftAssignmentRepository.findByIsOvertimeTrue();
        assertEquals(1, found.size());
        assertEquals("COMPLETED", found.get(0).getStatus());
    }

    @Test
    void testFindByStatus() {
        List<ShiftAssignment> found = shiftAssignmentRepository.findByStatus("CANCELLED");
        assertEquals(1, found.size());
        assertEquals("CANCELLED", found.get(0).getStatus());
    }

    @Test
    void testSaveAndFindAll() {
        List<ShiftAssignment> all = shiftAssignmentRepository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    void testDelete() {
        shiftAssignmentRepository.delete(assign1);
        List<ShiftAssignment> all = shiftAssignmentRepository.findAll();
        assertEquals(2, all.size());
        assertFalse(all.stream().anyMatch(a -> "ASSIGNED".equals(a.getStatus())));
    }

    @Test
    void testEdgeCases() {
        ShiftAssignment edge = new ShiftAssignment(null, emp1, template1, null, null, "", null, null);
        shiftAssignmentRepository.save(edge);
        List<ShiftAssignment> found = shiftAssignmentRepository.findByStatus("");
        assertTrue(found.size() >= 1);
    }
}

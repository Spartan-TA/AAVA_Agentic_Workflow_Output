package com.warehouse.employee;

import com.warehouse.employee.model.Employee;
import com.warehouse.employee.model.LeaveRequest;
import com.warehouse.employee.repository.LeaveRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.time.LocalDate;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LeaveRepositoryTest {
    @Autowired
    private LeaveRepository leaveRepository;

    private Employee emp1, emp2;
    private LeaveRequest req1, req2, req3;

    @BeforeEach
    void setUp() {
        emp1 = new Employee(); emp1.setId(1L); emp1.setFirstName("John");
        emp2 = new Employee(); emp2.setId(2L); emp2.setFirstName("Jane");
        req1 = new LeaveRequest(null, emp1, "PTO", LocalDate.of(2024,6,1), LocalDate.of(2024,6,5), "APPROVED", "Manager", "Vacation", null, null);
        req2 = new LeaveRequest(null, emp1, "SICK", LocalDate.of(2024,6,10), LocalDate.of(2024,6,12), "REQUESTED", "HR", "Flu", null, null);
        req3 = new LeaveRequest(null, emp2, "PTO", LocalDate.of(2024,6,3), LocalDate.of(2024,6,7), "REJECTED", "Manager", "No balance", null, null);
        leaveRepository.save(req1);
        leaveRepository.save(req2);
        leaveRepository.save(req3);
    }

    @Test
    void testFindByEmployeeId() {
        List<LeaveRequest> found = leaveRepository.findByEmployeeId(1L);
        assertEquals(2, found.size());
        assertTrue(found.stream().anyMatch(r -> "PTO".equals(r.getLeaveType())));
        assertTrue(found.stream().anyMatch(r -> "SICK".equals(r.getLeaveType())));
    }

    @Test
    void testFindByStatus() {
        List<LeaveRequest> found = leaveRepository.findByStatus("APPROVED");
        assertEquals(1, found.size());
        assertEquals("APPROVED", found.get(0).getStatus());
    }

    @Test
    void testFindByStatusOrderByRequestedAtAsc() {
        List<LeaveRequest> found = leaveRepository.findByStatusOrderByRequestedAtAsc("REQUESTED");
        assertEquals(1, found.size());
        assertEquals("REQUESTED", found.get(0).getStatus());
    }

    @Test
    void testFindOverlappingLeave() {
        List<LeaveRequest> found = leaveRepository.findOverlappingLeave(1L, LocalDate.of(2024,6,4), LocalDate.of(2024,6,11));
        assertEquals(2, found.size());
    }

    @Test
    void testFindApprovedLeaveInRange() {
        List<LeaveRequest> found = leaveRepository.findApprovedLeaveInRange(LocalDate.of(2024,6,1), LocalDate.of(2024,6,5));
        assertEquals(1, found.size());
        assertEquals("APPROVED", found.get(0).getStatus());
    }

    @Test
    void testSaveAndFindAll() {
        List<LeaveRequest> all = leaveRepository.findAll();
        assertEquals(3, all.size());
    }

    @Test
    void testDelete() {
        leaveRepository.delete(req1);
        List<LeaveRequest> all = leaveRepository.findAll();
        assertEquals(2, all.size());
        assertFalse(all.stream().anyMatch(r -> "APPROVED".equals(r.getStatus())));
    }

    @Test
    void testEdgeCases() {
        LeaveRequest edge = new LeaveRequest(null, emp1, "", null, null, "", "", "", null, null);
        leaveRepository.save(edge);
        List<LeaveRequest> found = leaveRepository.findByStatus("");
        assertTrue(found.size() >= 1);
    }
}

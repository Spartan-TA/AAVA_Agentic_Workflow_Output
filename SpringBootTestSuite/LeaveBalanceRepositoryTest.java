package com.warehouse.employee;

import com.warehouse.employee.model.Employee;
import com.warehouse.employee.model.LeaveBalance;
import com.warehouse.employee.repository.LeaveBalanceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.math.BigDecimal;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class LeaveBalanceRepositoryTest {
    @Autowired
    private LeaveBalanceRepository leaveBalanceRepository;

    private Employee emp1, emp2;
    private LeaveBalance bal1, bal2;

    @BeforeEach
    void setUp() {
        emp1 = new Employee(); emp1.setId(1L); emp1.setFirstName("John");
        emp2 = new Employee(); emp2.setId(2L); emp2.setFirstName("Jane");
        bal1 = new LeaveBalance(null, emp1, new BigDecimal("10.00"), new BigDecimal("5.00"), new BigDecimal("0.00"), null);
        bal2 = new LeaveBalance(null, emp2, new BigDecimal("2.00"), new BigDecimal("1.00"), new BigDecimal("0.00"), null);
        leaveBalanceRepository.save(bal1);
        leaveBalanceRepository.save(bal2);
    }

    @Test
    void testFindAll() {
        List<LeaveBalance> all = leaveBalanceRepository.findAll();
        assertEquals(2, all.size());
    }

    @Test
    void testSaveAndFindById() {
        LeaveBalance found = leaveBalanceRepository.findById(bal1.getId()).orElse(null);
        assertNotNull(found);
        assertEquals(bal1.getPtoBalance(), found.getPtoBalance());
    }

    @Test
    void testDelete() {
        leaveBalanceRepository.delete(bal1);
        List<LeaveBalance> all = leaveBalanceRepository.findAll();
        assertEquals(1, all.size());
        assertFalse(all.stream().anyMatch(b -> b.getEmployee().getFirstName().equals("John")));
    }

    @Test
    void testEdgeCases() {
        LeaveBalance edge = new LeaveBalance(null, emp1, null, null, null, null);
        leaveBalanceRepository.save(edge);
        List<LeaveBalance> all = leaveBalanceRepository.findAll();
        assertTrue(all.size() >= 2);
    }
}

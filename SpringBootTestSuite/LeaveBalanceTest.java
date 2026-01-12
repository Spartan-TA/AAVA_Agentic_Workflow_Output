package com.warehouse.employee;

import com.warehouse.employee.model.LeaveBalance;
import com.warehouse.employee.model.Employee;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class LeaveBalanceTest {
    @Test
    void testConstructorAndGetters() {
        Employee emp = new Employee();
        LocalDateTime now = LocalDateTime.now();
        LeaveBalance balance = new LeaveBalance(1L, emp, new BigDecimal("10.50"), new BigDecimal("5.25"), new BigDecimal("0.00"), now);
        assertEquals(1L, balance.getId());
        assertEquals(emp, balance.getEmployee());
        assertEquals(new BigDecimal("10.50"), balance.getPtoBalance());
        assertEquals(new BigDecimal("5.25"), balance.getSickBalance());
        assertEquals(new BigDecimal("0.00"), balance.getUnpaidBalance());
        assertEquals(now, balance.getUpdatedAt());
    }

    @Test
    void testDefaultValues() {
        LeaveBalance balance = new LeaveBalance();
        assertNull(balance.getId());
        assertNull(balance.getEmployee());
        assertEquals(BigDecimal.ZERO, balance.getPtoBalance());
        assertEquals(BigDecimal.ZERO, balance.getSickBalance());
        assertEquals(BigDecimal.ZERO, balance.getUnpaidBalance());
        assertNotNull(balance.getUpdatedAt());
    }

    @Test
    void testSetters() {
        LeaveBalance balance = new LeaveBalance();
        Employee emp = new Employee();
        LocalDateTime updated = LocalDateTime.of(2024, 7, 2, 8, 0);
        balance.setId(2L);
        balance.setEmployee(emp);
        balance.setPtoBalance(new BigDecimal("20.00"));
        balance.setSickBalance(new BigDecimal("2.00"));
        balance.setUnpaidBalance(new BigDecimal("1.00"));
        balance.setUpdatedAt(updated);
        assertEquals(2L, balance.getId());
        assertEquals(emp, balance.getEmployee());
        assertEquals(new BigDecimal("20.00"), balance.getPtoBalance());
        assertEquals(new BigDecimal("2.00"), balance.getSickBalance());
        assertEquals(new BigDecimal("1.00"), balance.getUnpaidBalance());
        assertEquals(updated, balance.getUpdatedAt());
    }

    @Test
    void testOnUpdateUpdatesUpdatedAt() {
        LeaveBalance balance = new LeaveBalance();
        LocalDateTime before = balance.getUpdatedAt();
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        balance.onUpdate();
        LocalDateTime after = balance.getUpdatedAt();
        assertTrue(after.isAfter(before));
    }

    @Test
    void testEdgeCases() {
        LeaveBalance balance = new LeaveBalance();
        balance.setPtoBalance(null);
        balance.setSickBalance(null);
        balance.setUnpaidBalance(null);
        assertNull(balance.getPtoBalance());
        assertNull(balance.getSickBalance());
        assertNull(balance.getUnpaidBalance());
    }
}

package com.warehouse.employee;

import com.warehouse.employee.model.ShiftTemplate;
import org.junit.jupiter.api.Test;
import java.time.LocalTime;
import java.time.LocalDateTime;
import static org.junit.jupiter.api.Assertions.*;

class ShiftTemplateTest {
    @Test
    void testShiftTemplateConstructorAndGetters() {
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(16, 0);
        LocalDateTime now = LocalDateTime.now();
        ShiftTemplate template = new ShiftTemplate(1L, "Morning Shift", start, end, "WEEKLY", true, "2024-12-25", 100L, now, now);
        assertEquals(1L, template.getId());
        assertEquals("Morning Shift", template.getName());
        assertEquals(start, template.getStartTime());
        assertEquals(end, template.getEndTime());
        assertEquals("WEEKLY", template.getRotationPattern());
        assertTrue(template.getOvertimeAllowed());
        assertEquals("2024-12-25", template.getBlackoutDates());
        assertEquals(100L, template.getTenantId());
        assertEquals(now, template.getCreatedAt());
        assertEquals(now, template.getUpdatedAt());
    }

    @Test
    void testDefaultValues() {
        ShiftTemplate template = new ShiftTemplate();
        assertNull(template.getId());
        assertNull(template.getName());
        assertNull(template.getStartTime());
        assertNull(template.getEndTime());
        assertNull(template.getRotationPattern());
        assertFalse(template.getOvertimeAllowed());
        assertNull(template.getBlackoutDates());
        assertNull(template.getTenantId());
        assertNotNull(template.getCreatedAt());
        assertNotNull(template.getUpdatedAt());
    }

    @Test
    void testSetters() {
        ShiftTemplate template = new ShiftTemplate();
        template.setId(2L);
        template.setName("Night Shift");
        template.setStartTime(LocalTime.of(22, 0));
        template.setEndTime(LocalTime.of(6, 0));
        template.setRotationPattern("MONTHLY");
        template.setOvertimeAllowed(false);
        template.setBlackoutDates("2024-01-01");
        template.setTenantId(200L);
        LocalDateTime created = LocalDateTime.of(2024, 1, 1, 0, 0);
        LocalDateTime updated = LocalDateTime.of(2024, 1, 2, 0, 0);
        template.setCreatedAt(created);
        template.setUpdatedAt(updated);
        assertEquals(2L, template.getId());
        assertEquals("Night Shift", template.getName());
        assertEquals(LocalTime.of(22, 0), template.getStartTime());
        assertEquals(LocalTime.of(6, 0), template.getEndTime());
        assertEquals("MONTHLY", template.getRotationPattern());
        assertFalse(template.getOvertimeAllowed());
        assertEquals("2024-01-01", template.getBlackoutDates());
        assertEquals(200L, template.getTenantId());
        assertEquals(created, template.getCreatedAt());
        assertEquals(updated, template.getUpdatedAt());
    }

    @Test
    void testOnUpdateUpdatesUpdatedAt() {
        ShiftTemplate template = new ShiftTemplate();
        LocalDateTime before = template.getUpdatedAt();
        try { Thread.sleep(10); } catch (InterruptedException ignored) {}
        template.onUpdate();
        LocalDateTime after = template.getUpdatedAt();
        assertTrue(after.isAfter(before));
    }

    @Test
    void testEdgeCases() {
        ShiftTemplate template = new ShiftTemplate();
        template.setName("");
        template.setRotationPattern(null);
        template.setBlackoutDates("");
        template.setOvertimeAllowed(null);
        assertEquals("", template.getName());
        assertNull(template.getRotationPattern());
        assertEquals("", template.getBlackoutDates());
        assertNull(template.getOvertimeAllowed());
    }
}

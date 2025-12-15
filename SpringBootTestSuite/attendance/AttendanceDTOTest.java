package com.example.ems.attendance;

import com.example.ems.attendance.dto.AttendanceDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AttendanceDTOTest {

    @Test
    @DisplayName("getters and setters - Normal Case")
    void testGettersAndSetters() {
        AttendanceDTO dto = new AttendanceDTO();
        dto.setEmployeeId(1L);
        dto.setClockInTime("2023-01-01T08:00:00");
        dto.setClockOutTime("2023-01-01T17:00:00");
        dto.setDate(LocalDate.of(2023, 1, 1));
        assertEquals(1L, dto.getEmployeeId());
        assertEquals("2023-01-01T08:00:00", dto.getClockInTime());
        assertEquals("2023-01-01T17:00:00", dto.getClockOutTime());
        assertEquals(LocalDate.of(2023, 1, 1), dto.getDate());
    }

    @Test
    @DisplayName("equals and hashCode - Same Values")
    void testEqualsAndHashCodeSameValues() {
        AttendanceDTO a = new AttendanceDTO();
        a.setEmployeeId(1L);
        a.setDate(LocalDate.of(2023, 1, 1));
        AttendanceDTO b = new AttendanceDTO();
        b.setEmployeeId(1L);
        b.setDate(LocalDate.of(2023, 1, 1));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("toString - Not Null")
    void testToStringNotNull() {
        AttendanceDTO dto = new AttendanceDTO();
        assertNotNull(dto.toString());
    }
}

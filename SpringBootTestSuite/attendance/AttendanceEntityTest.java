package com.example.ems.attendance;

import com.example.ems.attendance.entity.AttendanceEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class AttendanceEntityTest {

    @Test
    @DisplayName("equals and hashCode - Same Values")
    void testEqualsAndHashCodeSameValues() {
        AttendanceEntity a = new AttendanceEntity();
        a.setId(1L);
        a.setEmployeeId(2L);
        a.setDate(LocalDate.of(2023, 1, 1));
        AttendanceEntity b = new AttendanceEntity();
        b.setId(1L);
        b.setEmployeeId(2L);
        b.setDate(LocalDate.of(2023, 1, 1));
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    @DisplayName("equals - Different Values")
    void testEqualsDifferentValues() {
        AttendanceEntity a = new AttendanceEntity();
        a.setId(1L);
        AttendanceEntity b = new AttendanceEntity();
        b.setId(2L);
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("toString - Not Null")
    void testToStringNotNull() {
        AttendanceEntity a = new AttendanceEntity();
        a.setId(1L);
        assertNotNull(a.toString());
    }
}

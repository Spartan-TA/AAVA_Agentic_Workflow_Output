package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import java.time.LocalDateTime;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive unit tests for AttendanceEvent entity
 * Tests cover: entity fields, builder pattern, clock-in/out events, validation
 */
class AttendanceEventTest {

    @Test
    @DisplayName("Should create attendance event with all fields")
    void testAttendanceEventFieldsAndBuilder() {
        // Arrange
        Employee employee = Employee.builder().id(1L).name("John Doe").build();
        LocalDateTime timestamp = LocalDateTime.now();

        // Act
        AttendanceEvent event = AttendanceEvent.builder()
                .id(1L)
                .employee(employee)
                .timestamp(timestamp)
                .type(EventType.IN)
                .deviceId("DEVICE123")
                .location("Warehouse A")
                .build();

        // Assert
        assertThat(event.getId()).isEqualTo(1L);
        assertThat(event.getEmployee()).isEqualTo(employee);
        assertThat(event.getTimestamp()).isEqualTo(timestamp);
        assertThat(event.getType()).isEqualTo(EventType.IN);
        assertThat(event.getDeviceId()).isEqualTo("DEVICE123");
        assertThat(event.getLocation()).isEqualTo("Warehouse A");
    }

    @Test
    @DisplayName("Should create clock-in event")
    void testClockInEvent() {
        // Arrange & Act
        AttendanceEvent clockIn = AttendanceEvent.builder()
                .type(EventType.IN)
                .timestamp(LocalDateTime.now())
                .build();

        // Assert
        assertThat(clockIn.getType()).isEqualTo(EventType.IN);
        assertThat(clockIn.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Should create clock-out event")
    void testClockOutEvent() {
        // Arrange & Act
        AttendanceEvent clockOut = AttendanceEvent.builder()
                .type(EventType.OUT)
                .timestamp(LocalDateTime.now())
                .build();

        // Assert
        assertThat(clockOut.getType()).isEqualTo(EventType.OUT);
    }

    @Test
    @DisplayName("Should handle null location")
    void testNullLocation() {
        // Arrange & Act
        AttendanceEvent event = AttendanceEvent.builder()
                .type(EventType.IN)
                .location(null)
                .build();

        // Assert
        assertThat(event.getLocation()).isNull();
    }

    @Test
    @DisplayName("Should handle null device ID")
    void testNullDeviceId() {
        // Arrange & Act
        AttendanceEvent event = AttendanceEvent.builder()
                .type(EventType.IN)
                .deviceId(null)
                .build();

        // Assert
        assertThat(event.getDeviceId()).isNull();
    }

    @Test
    @DisplayName("Should test equals and hashCode")
    void testEqualsAndHashCode() {
        // Arrange
        LocalDateTime time = LocalDateTime.now();
        AttendanceEvent e1 = AttendanceEvent.builder().id(1L).timestamp(time).build();
        AttendanceEvent e2 = AttendanceEvent.builder().id(1L).timestamp(time).build();
        AttendanceEvent e3 = AttendanceEvent.builder().id(2L).timestamp(time).build();

        // Assert
        assertThat(e1).isEqualTo(e2);
        assertThat(e1).hasSameHashCodeAs(e2);
        assertThat(e1).isNotEqualTo(e3);
    }

    @Test
    @DisplayName("Should capture geolocation data")
    void testGeolocationCapture() {
        // Arrange & Act
        AttendanceEvent event = AttendanceEvent.builder()
                .type(EventType.IN)
                .location("40.7128,-74.0060") // NYC coordinates
                .build();

        // Assert
        assertThat(event.getLocation()).isEqualTo("40.7128,-74.0060");
    }

    @Test
    @DisplayName("Should handle overnight shift boundary")
    void testOvernightShiftBoundary() {
        // Arrange
        LocalDateTime clockIn = LocalDateTime.of(2024, 1, 15, 23, 0);
        LocalDateTime clockOut = LocalDateTime.of(2024, 1, 16, 7, 0);

        // Act
        AttendanceEvent inEvent = AttendanceEvent.builder()
                .type(EventType.IN)
                .timestamp(clockIn)
                .build();
        AttendanceEvent outEvent = AttendanceEvent.builder()
                .type(EventType.OUT)
                .timestamp(clockOut)
                .build();

        // Assert
        assertThat(inEvent.getTimestamp()).isBefore(outEvent.getTimestamp());
        assertThat(inEvent.getTimestamp().toLocalDate())
                .isNotEqualTo(outEvent.getTimestamp().toLocalDate());
    }
}
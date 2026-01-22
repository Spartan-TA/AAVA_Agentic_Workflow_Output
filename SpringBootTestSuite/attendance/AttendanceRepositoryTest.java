package com.warehouse.ems.attendance;

import com.warehouse.ems.employee.Employee;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprehensive repository tests for Attendance
 * Tests cover: CRUD operations, custom queries, date filtering, employee associations
 */
@DataJpaTest
class AttendanceRepositoryTest {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("Should save and retrieve attendance event by ID")
    void testSaveAndFindById() {
        // Arrange
        Employee employee = Employee.builder()
                .name("John Doe")
                .badgeId("B123")
                .build();
        entityManager.persist(employee);
        
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .deviceId("DEVICE123")
                .location("Warehouse A")
                .build();

        // Act
        AttendanceEvent saved = attendanceRepository.save(event);
        Optional<AttendanceEvent> found = attendanceRepository.findById(saved.getId());

        // Assert
        assertThat(found).isPresent();
        assertThat(found.get().getEmployee().getName()).isEqualTo("John Doe");
        assertThat(found.get().getType()).isEqualTo(EventType.IN);
    }

    @Test
    @DisplayName("Should find latest event by employee")
    void testFindLatestByEmployee() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Jane Smith")
                .badgeId("B456")
                .build();
        entityManager.persist(employee);
        
        AttendanceEvent event1 = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now().minusHours(2))
                .type(EventType.IN)
                .build();
        AttendanceEvent event2 = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.OUT)
                .build();
        
        attendanceRepository.save(event1);
        attendanceRepository.save(event2);

        // Act
        Optional<AttendanceEvent> latest = attendanceRepository.findLatestByEmployee(employee.getId());

        // Assert
        assertThat(latest).isPresent();
        assertThat(latest.get().getType()).isEqualTo(EventType.OUT);
    }

    @Test
    @DisplayName("Should find events by employee and date")
    void testFindByEmployeeAndDate() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Bob Johnson")
                .badgeId("B789")
                .build();
        entityManager.persist(employee);
        
        LocalDate today = LocalDate.now();
        AttendanceEvent todayEvent = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(today.atTime(9, 0))
                .type(EventType.IN)
                .build();
        AttendanceEvent yesterdayEvent = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(today.minusDays(1).atTime(9, 0))
                .type(EventType.IN)
                .build();
        
        attendanceRepository.save(todayEvent);
        attendanceRepository.save(yesterdayEvent);

        // Act
        List<AttendanceEvent> events = attendanceRepository.findByEmployeeAndDate(employee.getId(), today);

        // Assert
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getTimestamp().toLocalDate()).isEqualTo(today);
    }

    @Test
    @DisplayName("Should find events by date range")
    void testFindByDateRange() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Alice Brown")
                .badgeId("B101")
                .build();
        entityManager.persist(employee);
        
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        
        AttendanceEvent event1 = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(start.plusDays(1))
                .type(EventType.IN)
                .build();
        AttendanceEvent event2 = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(start.plusDays(2))
                .type(EventType.OUT)
                .build();
        AttendanceEvent event3 = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(start.minusDays(1)) // Outside range
                .type(EventType.IN)
                .build();
        
        attendanceRepository.save(event1);
        attendanceRepository.save(event2);
        attendanceRepository.save(event3);

        // Act
        List<AttendanceEvent> events = attendanceRepository.findByDateRange(start, end);

        // Assert
        assertThat(events).hasSize(2);
    }

    @Test
    @DisplayName("Should handle empty repository")
    void testEmptyRepository() {
        // Act
        List<AttendanceEvent> all = attendanceRepository.findAll();

        // Assert
        assertThat(all).isEmpty();
    }

    @Test
    @DisplayName("Should delete attendance event")
    void testDeleteEvent() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Charlie Davis")
                .badgeId("B202")
                .build();
        entityManager.persist(employee);
        
        AttendanceEvent event = AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .build();
        AttendanceEvent saved = attendanceRepository.save(event);
        Long id = saved.getId();

        // Act
        attendanceRepository.deleteById(id);

        // Assert
        assertThat(attendanceRepository.findById(id)).isEmpty();
    }

    @Test
    @DisplayName("Should find all clock-in events")
    void testFindAllClockInEvents() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Diana Evans")
                .badgeId("B303")
                .build();
        entityManager.persist(employee);
        
        attendanceRepository.save(AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .build());
        attendanceRepository.save(AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now().plusHours(8))
                .type(EventType.OUT)
                .build());

        // Act
        List<AttendanceEvent> clockIns = attendanceRepository.findByType(EventType.IN);

        // Assert
        assertThat(clockIns).hasSize(1);
        assertThat(clockIns.get(0).getType()).isEqualTo(EventType.IN);
    }

    @Test
    @DisplayName("Should find events by device ID")
    void testFindByDeviceId() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Frank Green")
                .badgeId("B404")
                .build();
        entityManager.persist(employee);
        
        attendanceRepository.save(AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .deviceId("DEVICE123")
                .build());
        attendanceRepository.save(AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .deviceId("DEVICE456")
                .build());

        // Act
        List<AttendanceEvent> events = attendanceRepository.findByDeviceId("DEVICE123");

        // Assert
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getDeviceId()).isEqualTo("DEVICE123");
    }

    @Test
    @DisplayName("Should find events by location")
    void testFindByLocation() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Grace Harris")
                .badgeId("B505")
                .build();
        entityManager.persist(employee);
        
        attendanceRepository.save(AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .location("Warehouse A")
                .build());
        attendanceRepository.save(AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .location("Warehouse B")
                .build());

        // Act
        List<AttendanceEvent> events = attendanceRepository.findByLocation("Warehouse A");

        // Assert
        assertThat(events).hasSize(1);
        assertThat(events.get(0).getLocation()).isEqualTo("Warehouse A");
    }

    @Test
    @DisplayName("Should count events by employee")
    void testCountByEmployee() {
        // Arrange
        Employee employee = Employee.builder()
                .name("Henry Irving")
                .badgeId("B606")
                .build();
        entityManager.persist(employee);
        
        attendanceRepository.save(AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now())
                .type(EventType.IN)
                .build());
        attendanceRepository.save(AttendanceEvent.builder()
                .employee(employee)
                .timestamp(LocalDateTime.now().plusHours(8))
                .type(EventType.OUT)
                .build());

        // Act
        long count = attendanceRepository.countByEmployee(employee.getId());

        // Assert
        assertThat(count).isEqualTo(2);
    }
}
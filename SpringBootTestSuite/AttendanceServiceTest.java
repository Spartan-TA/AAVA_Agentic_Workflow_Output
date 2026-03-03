import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class AttendanceServiceTest {
    @Mock
    private AttendanceEventRepository repository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private AttendanceService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should clock in employee")
    void testClockIn_NormalCase() {
        Employee employee = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        AttendanceEvent event = new AttendanceEvent(null, employee, LocalDateTime.now(), null, "D1", "Main", false);

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(repository.save(any(AttendanceEvent.class))).thenReturn(event);

        AttendanceEvent result = service.clockIn(1L, "D1", "Main");

        assertNotNull(result);
        assertEquals("D1", result.getDeviceId());
        assertEquals(employee, result.getEmployee());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown employee")
    void testClockIn_UnknownEmployee() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.clockIn(99L, "D1", "Main"));
    }

    @Test
    @DisplayName("Should clock out employee")
    void testClockOut_NormalCase() {
        Employee employee = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        AttendanceEvent event = new AttendanceEvent(1L, employee, LocalDateTime.now().minusHours(8), null, "D1", "Main", false);

        when(repository.findByEmployeeIdAndClockOutIsNull(1L)).thenReturn(Optional.of(event));
        when(repository.save(any(AttendanceEvent.class))).thenReturn(event);

        AttendanceEvent result = service.clockOut(1L);

        assertNotNull(result.getClockOut());
        assertTrue(result.getClockOut().isAfter(event.getClockIn()));
    }

    @Test
    @DisplayName("Should throw BusinessException for already clocked out")
    void testClockOut_AlreadyClockedOut() {
        when(repository.findByEmployeeIdAndClockOutIsNull(1L)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.clockOut(1L));
    }

    @Test
    @DisplayName("Should calculate hours worked")
    void testCalculateHoursWorked_NormalCase() {
        Employee employee = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        AttendanceEvent event = new AttendanceEvent(1L, employee, LocalDateTime.now().minusHours(8), LocalDateTime.now(), "D1", "Main", false);

        when(repository.findByEmployeeAndClockInBetween(eq(employee), any(), any())).thenReturn(Arrays.asList(event));

        double hours = service.calculateHoursWorked(employee, LocalDate.now(), LocalDate.now());

        assertTrue(hours >= 8.0);
    }

    @Test
    @DisplayName("Should handle empty attendance events")
    void testCalculateHoursWorked_EmptyEvents() {
        Employee employee = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);

        when(repository.findByEmployeeAndClockInBetween(eq(employee), any(), any())).thenReturn(Collections.emptyList());

        double hours = service.calculateHoursWorked(employee, LocalDate.now(), LocalDate.now());

        assertEquals(0.0, hours);
    }

    @Test
    @DisplayName("Should request correction")
    void testRequestCorrection_NormalCase() {
        AttendanceEvent event = new AttendanceEvent(1L, null, LocalDateTime.now().minusHours(8), LocalDateTime.now(), "D1", "Main", false);

        when(repository.findById(1L)).thenReturn(Optional.of(event));
        when(repository.save(any(AttendanceEvent.class))).thenReturn(event);

        AttendanceEvent result = service.requestCorrection(1L);

        assertTrue(result.isCorrectionRequested());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for correction request")
    void testRequestCorrection_ResourceNotFound() {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.requestCorrection(99L));
    }
}
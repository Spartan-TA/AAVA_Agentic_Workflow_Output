import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class ScheduleServiceTest {
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private ScheduleService scheduleService;
    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    void testCreateShiftTemplate_ValidInput() {
        ShiftTemplate template = new ShiftTemplate("Morning", "08:00", "16:00", "Shipping");
        when(scheduleRepository.save(any(ShiftTemplate.class))).thenReturn(template);
        ShiftTemplate result = scheduleService.createShiftTemplate(template);
        assertEquals("Morning", result.getName());
    }

    @Test
    void testCreateShiftTemplate_NullInput() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.createShiftTemplate(null));
    }

    @Test
    void testAssignShiftToEmployee_ValidInput() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        ShiftAssignment assignment = new ShiftAssignment(employee, "Morning", new Date());
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(scheduleRepository.save(any(ShiftAssignment.class))).thenReturn(assignment);
        ShiftAssignment result = scheduleService.assignShiftToEmployee("B123", "Morning", new Date());
        assertEquals("Morning", result.getShiftName());
    }

    @Test
    void testAssignShiftToEmployee_ConflictDetection() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(scheduleService.hasShiftConflict("B123", new Date())).thenReturn(true);
        assertThrows(SchedulingConflictException.class, () -> scheduleService.assignShiftToEmployee("B123", "Morning", new Date()));
    }

    @Test
    void testAssignShiftToEmployee_InvalidBadgeId() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> scheduleService.assignShiftToEmployee("BADGE999", "Morning", new Date()));
    }

    @Test
    void testAssignShiftToEmployee_NullShiftName() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        assertThrows(IllegalArgumentException.class, () -> scheduleService.assignShiftToEmployee("B123", null, new Date()));
    }

    @Test
    void testListEmployeeShifts_ValidInput() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        List<ShiftAssignment> assignments = Arrays.asList(
            new ShiftAssignment(employee, "Morning", new Date()),
            new ShiftAssignment(employee, "Evening", new Date())
        );
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(scheduleRepository.findByEmployeeBadgeId("B123")).thenReturn(assignments);
        List<ShiftAssignment> result = scheduleService.listEmployeeShifts("B123");
        assertEquals(2, result.size());
    }

    @Test
    void testListEmployeeShifts_InvalidBadgeId() {
        when(employeeRepository.findByBadgeId("BADGE999")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> scheduleService.listEmployeeShifts("BADGE999"));
    }

    @Test
    void testCreateShiftTemplate_BoundaryValues() {
        ShiftTemplate minTemplate = new ShiftTemplate("A", "00:00", "01:00", "Shipping");
        ShiftTemplate maxTemplate = new ShiftTemplate("A very long shift name exceeding normal limits", "08:00", "16:00", "Shipping");
        when(scheduleRepository.save(any(ShiftTemplate.class))).thenReturn(minTemplate).thenReturn(maxTemplate);
        assertDoesNotThrow(() -> scheduleService.createShiftTemplate(minTemplate));
        assertDoesNotThrow(() -> scheduleService.createShiftTemplate(maxTemplate));
    }

    @Test
    void testAssignShiftToEmployee_BlackoutDate() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(employeeRepository.findByBadgeId("B123")).thenReturn(Optional.of(employee));
        when(scheduleService.isBlackoutDate(any(Date.class))).thenReturn(true);
        assertThrows(BlackoutDateException.class, () -> scheduleService.assignShiftToEmployee("B123", "Morning", new Date()));
    }
}

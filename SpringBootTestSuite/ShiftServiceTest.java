import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ShiftServiceTest {
    @Mock
    private ShiftTemplateRepository templateRepository;
    @Mock
    private ShiftAssignmentRepository assignmentRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService service;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create shift template")
    void testCreateTemplate_NormalCase() {
        ShiftTemplate template = new ShiftTemplate(null, "Morning", LocalTime.of(8, 0), LocalTime.of(16, 0), true);
        when(templateRepository.save(any(ShiftTemplate.class))).thenReturn(template);

        ShiftTemplate result = service.createTemplate(template);

        assertNotNull(result);
        assertEquals("Morning", result.getName());
    }

    @Test
    @DisplayName("Should assign shift to employee")
    void testAssignShift_NormalCase() {
        Employee employee = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        ShiftTemplate template = new ShiftTemplate(1L, "Morning", LocalTime.of(8, 0), LocalTime.of(16, 0), true);
        ShiftAssignment assignment = new ShiftAssignment(null, employee, template, LocalDate.now());

        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(templateRepository.findById(1L)).thenReturn(Optional.of(template));
        when(assignmentRepository.save(any(ShiftAssignment.class))).thenReturn(assignment);

        ShiftAssignment result = service.assignShift(1L, 1L, LocalDate.now());

        assertNotNull(result);
        assertEquals(employee, result.getEmployee());
        assertEquals(template, result.getShiftTemplate());
    }

    @Test
    @DisplayName("Should detect conflicts")
    void testDetectConflicts_NormalCase() {
        List<ShiftAssignment> assignments = Arrays.asList(
            new ShiftAssignment(1L, null, null, LocalDate.now()),
            new ShiftAssignment(2L, null, null, LocalDate.now())
        );
        when(assignmentRepository.findByDate(LocalDate.now())).thenReturn(assignments);

        boolean conflict = service.detectConflicts(LocalDate.now());

        assertTrue(conflict);
    }

    @Test
    @DisplayName("Should list upcoming shifts")
    void testListUpcomingShifts_NormalCase() {
        List<ShiftAssignment> assignments = Arrays.asList(
            new ShiftAssignment(1L, null, null, LocalDate.now().plusDays(1)),
            new ShiftAssignment(2L, null, null, LocalDate.now().plusDays(2))
        );
        when(assignmentRepository.findByEmployeeIdAndDateBetween(1L, LocalDate.now(), LocalDate.now().plusDays(7))).thenReturn(assignments);

        List<ShiftAssignment> result = service.listUpcomingShifts(1L, LocalDate.now(), LocalDate.now().plusDays(7));

        assertEquals(2, result.size());
    }

    @Test
    @DisplayName("Should handle null input for createTemplate")
    void testCreateTemplate_NullInput() {
        assertThrows(ValidationException.class, () -> service.createTemplate(null));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown employee in assignShift")
    void testAssignShift_UnknownEmployee() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.assignShift(99L, 1L, LocalDate.now()));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for unknown template in assignShift")
    void testAssignShift_UnknownTemplate() {
        Employee employee = new Employee(1L, "B123", "John Doe", "WORKER", "Shipping", "A", LocalDate.now(), EmployeeStatus.ACTIVE, false);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(templateRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.assignShift(1L, 99L, LocalDate.now()));
    }

    @Test
    @DisplayName("Should handle empty assignments in detectConflicts")
    void testDetectConflicts_EmptyAssignments() {
        when(assignmentRepository.findByDate(LocalDate.now())).thenReturn(Collections.emptyList());

        boolean conflict = service.detectConflicts(LocalDate.now());

        assertFalse(conflict);
    }
}
package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class ShiftServiceTest {
    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private ShiftService shiftService;

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
    void testCreateShiftTemplate_Valid_Success() {
        ShiftTemplate template = new ShiftTemplate("Morning", "08:00", "16:00", "WEEKDAY");
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(template);
        ShiftTemplate result = shiftService.createShiftTemplate(template);
        assertNotNull(result);
        assertEquals("Morning", result.getName());
    }

    @Test
    void testCreateShiftTemplate_DuplicateName_ThrowsException() {
        ShiftTemplate template = new ShiftTemplate("Morning", "08:00", "16:00", "WEEKDAY");
        when(shiftRepository.existsByName("Morning")).thenReturn(true);
        assertThrows(DuplicateShiftTemplateException.class, () -> shiftService.createShiftTemplate(template));
    }

    @Test
    void testAssignEmployeeToShift_ConflictDetected_ThrowsException() {
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Shift shift1 = new Shift(1L, "Morning", "08:00", "16:00");
        Shift shift2 = new Shift(2L, "Morning", "08:00", "16:00");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift1));
        when(shiftRepository.findConflictingShifts(1L, "08:00", "16:00")).thenReturn(Arrays.asList(shift2));
        assertThrows(ShiftConflictException.class, () -> shiftService.assignEmployeeToShift(1L, 1L));
    }

    @Test
    void testAssignEmployeeToShift_Valid_Success() {
        Employee employee = new Employee("Jane Doe", "B124", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Shift shift = new Shift(1L, "Morning", "08:00", "16:00");
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(shiftRepository.findConflictingShifts(2L, "08:00", "16:00")).thenReturn(Collections.emptyList());
        shiftService.assignEmployeeToShift(2L, 1L);
        verify(shiftRepository).assignEmployeeToShift(2L, 1L);
    }

    @Test
    void testDeleteShiftTemplate_Valid_Success() {
        ShiftTemplate template = new ShiftTemplate("Night", "22:00", "06:00", "WEEKEND");
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(template));
        shiftService.deleteShiftTemplate(1L);
        verify(shiftRepository).deleteById(1L);
    }

    @Test
    void testDeleteShiftTemplate_InvalidId_ThrowsException() {
        when(shiftRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ShiftTemplateNotFoundException.class, () -> shiftService.deleteShiftTemplate(99L));
    }

    @Test
    void testListShiftTemplates_Pagination() {
        List<ShiftTemplate> templates = Arrays.asList(
            new ShiftTemplate("Morning", "08:00", "16:00", "WEEKDAY"),
            new ShiftTemplate("Night", "22:00", "06:00", "WEEKEND")
        );
        when(shiftRepository.findAll(any())).thenReturn(templates);
        List<ShiftTemplate> result = shiftService.listShiftTemplates(0, 10);
        assertEquals(2, result.size());
    }

    @Test
    void testCreateShiftTemplate_NullName_ThrowsException() {
        ShiftTemplate template = new ShiftTemplate(null, "08:00", "16:00", "WEEKDAY");
        assertThrows(InvalidShiftTemplateException.class, () -> shiftService.createShiftTemplate(template));
    }

    // Integration scenario: Bulk assign employees to shift
    @Test
    void testBulkAssignEmployeesToShift_Success() {
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        Shift shift = new Shift(1L, "Morning", "08:00", "16:00");
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        shiftService.bulkAssignEmployeesToShift(employeeIds, 1L);
        verify(shiftRepository).bulkAssignEmployeesToShift(employeeIds, 1L);
    }
}

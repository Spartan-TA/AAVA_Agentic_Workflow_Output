package SpringBootTestSuite;

import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import java.time.LocalTime;
import java.util.*;

/**
 * ShiftServiceTest - Comprehensive unit tests for ShiftService covering templates, assignment, conflicts, overtime, and edge cases.
 */
public class ShiftServiceTest {
    private ShiftService shiftService;

    @BeforeEach
    public void setUp() {
        shiftService = new ShiftService();
    }

    @Test
    public void testCreateShiftTemplateValid() {
        ShiftTemplate template = new ShiftTemplate("Morning", LocalTime.of(8,0), LocalTime.of(16,0));
        assertDoesNotThrow(() -> shiftService.createShiftTemplate(template));
    }

    @Test
    public void testCreateShiftTemplateInvalidTimes() {
        ShiftTemplate template = new ShiftTemplate("Invalid", LocalTime.of(16,0), LocalTime.of(8,0));
        assertThrows(IllegalArgumentException.class, () -> shiftService.createShiftTemplate(template));
    }

    @Test
    public void testAssignShiftToValidEmployee() {
        Shift shift = new Shift(1, LocalTime.of(8,0), LocalTime.of(16,0));
        Employee emp = new Employee(100, "John Doe");
        assertTrue(shiftService.assignShiftToEmployee(shift, emp));
    }

    @Test
    public void testAssignShiftToInvalidEmployee() {
        Shift shift = new Shift(1, LocalTime.of(8,0), LocalTime.of(16,0));
        Employee emp = null;
        assertFalse(shiftService.assignShiftToEmployee(shift, emp));
    }

    @Test
    public void testDetectConflictsOverlappingShifts() {
        Shift s1 = new Shift(1, LocalTime.of(8,0), LocalTime.of(16,0));
        Shift s2 = new Shift(2, LocalTime.of(15,0), LocalTime.of(23,0));
        List<Shift> shifts = Arrays.asList(s1, s2);
        assertTrue(shiftService.detectConflicts(shifts));
    }

    @Test
    public void testDetectConflictsNoOverlap() {
        Shift s1 = new Shift(1, LocalTime.of(8,0), LocalTime.of(16,0));
        Shift s2 = new Shift(2, LocalTime.of(16,0), LocalTime.of(23,0));
        List<Shift> shifts = Arrays.asList(s1, s2);
        assertFalse(shiftService.detectConflicts(shifts));
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 8, 24})
    public void testCalculateOvertimeVariousHours(int hoursWorked) {
        int overtime = shiftService.calculateOvertime(hoursWorked);
        if (hoursWorked > 8) {
            assertEquals(hoursWorked - 8, overtime);
        } else {
            assertEquals(0, overtime);
        }
    }

    @Test
    public void testBulkAssignShiftsMultipleEmployees() {
        Shift shift = new Shift(1, LocalTime.of(8,0), LocalTime.of(16,0));
        List<Employee> employees = Arrays.asList(new Employee(101, "A"), new Employee(102, "B"));
        assertEquals(2, shiftService.bulkAssignShifts(shift, employees));
    }

    @Test
    public void testGetShiftsByEmployee() {
        Employee emp = new Employee(103, "C");
        List<Shift> shifts = shiftService.getShiftsByEmployee(emp);
        assertNotNull(shifts);
    }

    @Test
    public void testUpdateShiftTemplate() {
        ShiftTemplate template = new ShiftTemplate("Evening", LocalTime.of(16,0), LocalTime.of(0,0));
        assertTrue(shiftService.updateShiftTemplate(template));
    }

    @Test
    public void testDeleteShiftSoftDelete() {
        Shift shift = new Shift(2, LocalTime.of(16,0), LocalTime.of(0,0));
        assertTrue(shiftService.deleteShift(shift));
        assertTrue(shift.isDeleted());
    }

    @ParameterizedTest
    @ValueSource(strings = {"00:00", "23:59"})
    public void testBoundaryShiftTimes(String timeStr) {
        LocalTime time = LocalTime.parse(timeStr);
        ShiftTemplate template = new ShiftTemplate("Boundary", time, time.plusHours(8));
        assertDoesNotThrow(() -> shiftService.createShiftTemplate(template));
    }

    @Test
    public void testNullInputs() {
        assertThrows(NullPointerException.class, () -> shiftService.createShiftTemplate(null));
        assertThrows(NullPointerException.class, () -> shiftService.assignShiftToEmployee(null, null));
    }

    @Test
    public void testEmptyEmployeeListBulkAssign() {
        Shift shift = new Shift(3, LocalTime.of(8,0), LocalTime.of(16,0));
        List<Employee> employees = Collections.emptyList();
        assertEquals(0, shiftService.bulkAssignShifts(shift, employees));
    }

    @Test
    public void testInvalidDateShiftTemplate() {
        ShiftTemplate template = new ShiftTemplate("InvalidDate", LocalTime.of(8,0), null);
        assertThrows(IllegalArgumentException.class, () -> shiftService.createShiftTemplate(template));
    }
}

package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.*;

import java.time.LocalTime;
import java.util.*;

class ShiftServiceTest {

    @Mock
    private ShiftRepository shiftRepository;

    @InjectMocks
    private ShiftService shiftService;

    private ShiftTemplate validShift;
    private ShiftAssignment validAssignment;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validShift = new ShiftTemplate(1L, "Morning Shift", LocalTime.of(8, 0), LocalTime.of(16, 0), "DAILY");
        validAssignment = new ShiftAssignment(1L, 1L, 1L, new Date(), new Date());
    }

    @AfterEach
    void tearDown() {
        // Clean up resources if needed
    }

    @Test
    void testCreateShiftTemplate_ValidInput() {
        when(shiftRepository.save(any(ShiftTemplate.class))).thenReturn(validShift);
        ShiftTemplate result = shiftService.createShiftTemplate(validShift);
        assertNotNull(result);
        assertEquals("Morning Shift", result.getName());
    }

    @Test
    void testCreateShiftTemplate_NullName() {
        ShiftTemplate nullShift = new ShiftTemplate(null, null, LocalTime.of(8, 0), LocalTime.of(16, 0), "DAILY");
        assertThrows(IllegalArgumentException.class, () -> shiftService.createShiftTemplate(nullShift));
    }

    @Test
    void testAssignShift_ValidInput() {
        when(shiftRepository.saveAssignment(any(ShiftAssignment.class))).thenReturn(validAssignment);
        ShiftAssignment result = shiftService.assignShift(1L, 1L, new Date(), new Date());
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    void testAssignShift_ConflictDetection() {
        when(shiftRepository.findConflictingAssignments(anyLong(), any(), any()))
            .thenReturn(Arrays.asList(validAssignment));
        assertThrows(IllegalStateException.class, () -> shiftService.assignShift(1L, 1L, new Date(), new Date()));
    }

    @Test
    void testBulkAssignShifts_ValidInput() {
        List<Long> employeeIds = Arrays.asList(1L, 2L, 3L);
        when(shiftRepository.saveAssignment(any(ShiftAssignment.class))).thenReturn(validAssignment);
        List<ShiftAssignment> results = shiftService.bulkAssignShifts(employeeIds, 1L, new Date(), new Date());
        assertNotNull(results);
        assertEquals(3, results.size());
    }

    @Test
    void testBulkAssignShifts_EmptyList() {
        List<Long> employeeIds = Collections.emptyList();
        assertThrows(IllegalArgumentException.class, () -> shiftService.bulkAssignShifts(employeeIds, 1L, new Date(), new Date()));
    }

    @Test
    void testGetShiftsByEmployee_ValidId() {
        when(shiftRepository.findByEmployeeId(1L)).thenReturn(Arrays.asList(validAssignment));
        List<ShiftAssignment> results = shiftService.getShiftsByEmployee(1L);
        assertNotNull(results);
        assertEquals(1, results.size());
    }

    @Test
    void testGetShiftsByEmployee_NoShifts() {
        when(shiftRepository.findByEmployeeId(999L)).thenReturn(Collections.emptyList());
        List<ShiftAssignment> results = shiftService.getShiftsByEmployee(999L);
        assertNotNull(results);
        assertTrue(results.isEmpty());
    }

    @Test
    void testAssignShift_BlackoutDate() {
        when(shiftRepository.isBlackoutDate(any())).thenReturn(true);
        assertThrows(IllegalStateException.class, () -> shiftService.assignShift(1L, 1L, new Date(), new Date()));
    }

    @Test
    void testAssignShift_OvertimeExceeded() {
        when(shiftRepository.calculateWeeklyHours(anyLong(), any())).thenReturn(50.0);
        assertThrows(IllegalStateException.class, () -> shiftService.assignShift(1L, 1L, new Date(), new Date()));
    }
}
import org.junit.jupiter.api.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ScheduleServiceImplTest {
    @Mock
    private ScheduleRepository scheduleRepository;
    @Mock
    private ShiftRepository shiftRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private ScheduleServiceImpl scheduleService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("testAssignShift_ValidInput_AssignsShift")
    void testAssignShift_ValidInput_AssignsShift() {
        Employee employee = new Employee("John", "Doe", "12345", "john.doe@email.com");
        Shift shift = new Shift("Morning", LocalDateTime.now(), LocalDateTime.now().plusHours(8));
        Schedule schedule = new Schedule(employee, shift);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(shift));
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(schedule);
        Schedule result = scheduleService.assignShift(1L, 1L);
        assertEquals(schedule, result);
    }

    @Test
    @DisplayName("testAssignShift_InvalidEmployeeId_ThrowsEmployeeNotFoundException")
    void testAssignShift_InvalidEmployeeId_ThrowsEmployeeNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> scheduleService.assignShift(99L, 1L));
    }

    @Test
    @DisplayName("testAssignShift_InvalidShiftId_ThrowsShiftNotFoundException")
    void testAssignShift_InvalidShiftId_ThrowsShiftNotFoundException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(new Employee()));
        when(shiftRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ShiftNotFoundException.class, () -> scheduleService.assignShift(1L, 99L));
    }

    @Test
    @DisplayName("testGetScheduleByEmployee_ValidId_ReturnsScheduleList")
    void testGetScheduleByEmployee_ValidId_ReturnsScheduleList() {
        List<Schedule> schedules = Arrays.asList(new Schedule());
        when(scheduleRepository.findByEmployeeId(1L)).thenReturn(schedules);
        List<Schedule> result = scheduleService.getScheduleByEmployee(1L);
        assertEquals(schedules, result);
    }

    @Test
    @DisplayName("testGetScheduleByEmployee_InvalidId_ReturnsEmptyList")
    void testGetScheduleByEmployee_InvalidId_ReturnsEmptyList() {
        when(scheduleRepository.findByEmployeeId(99L)).thenReturn(Collections.emptyList());
        List<Schedule> result = scheduleService.getScheduleByEmployee(99L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("testDetectConflicts_OverlappingShifts_ReturnsConflictList")
    void testDetectConflicts_OverlappingShifts_ReturnsConflictList() {
        List<Schedule> conflicts = Arrays.asList(new Schedule());
        when(scheduleRepository.findConflicts(any(), any())).thenReturn(conflicts);
        List<Schedule> result = scheduleService.detectConflicts(LocalDate.now(), LocalDate.now().plusDays(1));
        assertEquals(conflicts, result);
    }

    @Test
    @DisplayName("testBulkAssignShifts_ValidInput_AssignsShifts")
    void testBulkAssignShifts_ValidInput_AssignsShifts() {
        List<Schedule> schedules = Arrays.asList(new Schedule());
        when(scheduleRepository.saveAll(anyList())).thenReturn(schedules);
        List<Schedule> result = scheduleService.bulkAssignShifts(schedules);
        assertEquals(schedules, result);
    }

    @Test
    @DisplayName("testBulkAssignShifts_EmptyList_ReturnsEmptyList")
    void testBulkAssignShifts_EmptyList_ReturnsEmptyList() {
        when(scheduleRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());
        List<Schedule> result = scheduleService.bulkAssignShifts(Collections.emptyList());
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("testGetScheduleByDateRange_ValidInput_ReturnsScheduleList")
    void testGetScheduleByDateRange_ValidInput_ReturnsScheduleList() {
        List<Schedule> schedules = Arrays.asList(new Schedule());
        when(scheduleRepository.findByDateRange(any(), any())).thenReturn(schedules);
        List<Schedule> result = scheduleService.getScheduleByDateRange(LocalDate.now(), LocalDate.now().plusDays(7));
        assertEquals(schedules, result);
    }

    @Test
    @DisplayName("testAssignShift_NullInput_ThrowsIllegalArgumentException")
    void testAssignShift_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.assignShift(null, null));
    }

    @Test
    @DisplayName("testDetectConflicts_NullDates_ThrowsIllegalArgumentException")
    void testDetectConflicts_NullDates_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.detectConflicts(null, null));
    }

    @Test
    @DisplayName("testBulkAssignShifts_NullInput_ThrowsIllegalArgumentException")
    void testBulkAssignShifts_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.bulkAssignShifts(null));
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }
}

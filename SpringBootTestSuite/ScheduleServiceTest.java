@SpringBootTest
public class ScheduleServiceTest {
    @MockBean private ShiftRepository shiftRepository;
    @MockBean private EmployeeRepository employeeRepository;
    @MockBean private ScheduleRepository scheduleRepository;
    @Autowired private ScheduleService scheduleService;

    private Employee testEmployee;
    private Shift testShift;
    private ShiftDto shiftDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(1L, "John Doe", "EMP001", null, null, EmployeeStatus.ACTIVE, LocalDate.now());
        testShift = new Shift(1L, "Morning", LocalTime.of(8,0), LocalTime.of(16,0), "Warehouse");
        shiftDto = new ShiftDto("Morning", LocalTime.of(8,0), LocalTime.of(16,0), "Warehouse");
    }

    @Test
    void testCreateShiftTemplate_ValidInput_Success() {
        when(shiftRepository.save(any(Shift.class))).thenReturn(testShift);
        Shift result = scheduleService.createShiftTemplate(shiftDto);
        assertNotNull(result);
        verify(shiftRepository).save(any(Shift.class));
    }

    @Test
    void testCreateShiftTemplate_NullDto_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> scheduleService.createShiftTemplate(null));
    }

    @Test
    void testAssignShift_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(scheduleRepository.existsConflict(anyLong(), any(), any(), any())).thenReturn(false);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(new Schedule());

        Schedule result = scheduleService.assignShift(1L, 1L, LocalDate.now());

        assertNotNull(result);
        verify(scheduleRepository).save(any(Schedule.class));
    }

    @Test
    void testAssignShift_Conflict_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(scheduleRepository.existsConflict(anyLong(), any(), any(), any())).thenReturn(true);

        assertThrows(SchedulingConflictException.class, () -> scheduleService.assignShift(1L, 1L, LocalDate.now()));
    }

    @Test
    void testBulkAssignShifts_ValidInput_Success() {
        List<ShiftAssignmentDto> assignments = List.of(new ShiftAssignmentDto(1L, 1L, LocalDate.now()));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(scheduleRepository.existsConflict(anyLong(), any(), any(), any())).thenReturn(false);
        when(scheduleRepository.save(any(Schedule.class))).thenReturn(new Schedule());

        List<Schedule> result = scheduleService.bulkAssignShifts(assignments);

        assertEquals(1, result.size());
    }

    @Test
    void testBulkAssignShifts_Conflict_ThrowsException() {
        List<ShiftAssignmentDto> assignments = List.of(new ShiftAssignmentDto(1L, 1L, LocalDate.now()));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(shiftRepository.findById(1L)).thenReturn(Optional.of(testShift));
        when(scheduleRepository.existsConflict(anyLong(), any(), any(), any())).thenReturn(true);

        assertThrows(SchedulingConflictException.class, () -> scheduleService.bulkAssignShifts(assignments));
    }

    @Test
    void testDetectConflicts_ValidInput_NoConflicts() {
        when(scheduleRepository.findConflicts(anyLong(), any(), any(), any())).thenReturn(List.of());
        List<Schedule> result = scheduleService.detectConflicts(1L, LocalDate.now(), LocalTime.of(8,0), LocalTime.of(16,0));
        assertTrue(result.isEmpty());
    }

    @Test
    void testGetEmployeeSchedule_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        List<Schedule> schedules = List.of(new Schedule());
        when(scheduleRepository.findByEmployeeAndDateRange(eq(testEmployee), any(), any())).thenReturn(schedules);

        List<Schedule> result = scheduleService.getEmployeeSchedule(1L, LocalDate.now(), LocalDate.now().plusDays(7));
        assertEquals(1, result.size());
    }

    @Test
    void testGetEmployeeSchedule_InvalidEmployee_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> scheduleService.getEmployeeSchedule(999L, LocalDate.now(), LocalDate.now()));
    }
}
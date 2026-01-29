@SpringBootTest
public class AttendanceServiceTest {
    @MockBean private AttendanceRepository attendanceRepository;
    @MockBean private EmployeeRepository employeeRepository;
    @MockBean private GeofenceValidator geofenceValidator;
    @Autowired private AttendanceService attendanceService;

    private Employee testEmployee;
    private Attendance testAttendance;
    private ClockInDto clockInDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(1L, "John Doe", "EMP001", null, null, EmployeeStatus.ACTIVE, LocalDate.now());
        testAttendance = new Attendance(1L, testEmployee, LocalDateTime.now(), null, null, 0.0);
        clockInDto = new ClockInDto(LocalDateTime.now(), "37.7749,-122.4194");
    }

    @Test
    void testClockIn_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceValidator.isWithinGeofence(anyString())).thenReturn(true);
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        Attendance result = attendanceService.clockIn(1L, clockInDto);

        assertNotNull(result);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void testClockIn_NullDto_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.clockIn(1L, null));
    }

    @Test
    void testClockIn_InvalidEmployeeId_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockIn(999L, clockInDto));
    }

    @Test
    void testClockIn_GeofenceViolation_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(geofenceValidator.isWithinGeofence(anyString())).thenReturn(false);
        assertThrows(GeofenceViolationException.class, () -> attendanceService.clockIn(1L, clockInDto));
    }

    @Test
    void testClockOut_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveByEmployeeId(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        Attendance result = attendanceService.clockOut(1L);

        assertNotNull(result);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void testClockOut_NoActiveAttendance_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(attendanceRepository.findActiveByEmployeeId(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.clockOut(1L));
    }

    @Test
    void testGetAttendanceByEmployee_Valid_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        List<Attendance> records = List.of(testAttendance);
        when(attendanceRepository.findByEmployeeAndDateRange(eq(testEmployee), any(), any())).thenReturn(records);

        List<Attendance> result = attendanceService.getAttendanceByEmployee(1L, LocalDate.now().minusDays(1), LocalDate.now());

        assertEquals(1, result.size());
    }

    @Test
    void testGetAttendanceByEmployee_InvalidEmployee_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> attendanceService.getAttendanceByEmployee(999L, LocalDate.now(), LocalDate.now()));
    }

    @Test
    void testCorrectAttendance_ValidInput_Success() {
        AttendanceCorrectionDto correctionDto = new AttendanceCorrectionDto(LocalDateTime.now(), LocalDateTime.now().plusHours(8), "Correction");
        when(attendanceRepository.findById(1L)).thenReturn(Optional.of(testAttendance));
        when(attendanceRepository.save(any(Attendance.class))).thenReturn(testAttendance);

        Attendance result = attendanceService.correctAttendance(1L, correctionDto);

        assertNotNull(result);
        verify(attendanceRepository).save(any(Attendance.class));
    }

    @Test
    void testCorrectAttendance_NullDto_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.correctAttendance(1L, null));
    }

    @Test
    void testCalculateHours_ValidAttendance_Success() {
        testAttendance.setClockIn(LocalDateTime.now().minusHours(8));
        testAttendance.setClockOut(LocalDateTime.now());
        double hours = attendanceService.calculateHours(testAttendance);
        assertTrue(hours >= 7.99 && hours <= 8.01);
    }

    @Test
    void testCalculateHours_NullAttendance_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> attendanceService.calculateHours(null));
    }
}
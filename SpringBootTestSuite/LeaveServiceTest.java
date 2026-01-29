@SpringBootTest
public class LeaveServiceTest {
    @MockBean private LeaveRepository leaveRepository;
    @MockBean private EmployeeRepository employeeRepository;
    @MockBean private ScheduleRepository scheduleRepository;
    @Autowired private LeaveService leaveService;

    private Employee testEmployee;
    private Leave testLeave;
    private LeaveRequestDto leaveRequestDto;

    @BeforeEach
    void setUp() {
        testEmployee = new Employee(1L, "John Doe", "EMP001", null, null, EmployeeStatus.ACTIVE, LocalDate.now());
        testLeave = new Leave(1L, testEmployee, LeaveType.PAID, LocalDate.now(), LocalDate.now().plusDays(2), LeaveStatus.PENDING, null);
        leaveRequestDto = new LeaveRequestDto(1L, LeaveType.PAID, LocalDate.now(), LocalDate.now().plusDays(2));
    }

    @Test
    void testRequestLeave_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        Leave result = leaveService.requestLeave(leaveRequestDto);

        assertNotNull(result);
        verify(leaveRepository).save(any(Leave.class));
    }

    @Test
    void testRequestLeave_NullDto_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> leaveService.requestLeave(null));
    }

    @Test
    void testApproveLeave_ValidInput_Success() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(new Employee(2L, "Manager", "EMP002", null, null, EmployeeStatus.ACTIVE, LocalDate.now())));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        Leave result = leaveService.approveLeave(1L, 2L);

        assertNotNull(result);
        assertEquals(LeaveStatus.APPROVED, result.getStatus());
    }

    @Test
    void testApproveLeave_InvalidLeaveId_ThrowsException() {
        when(leaveRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.approveLeave(999L, 2L));
    }

    @Test
    void testDenyLeave_ValidInput_Success() {
        when(leaveRepository.findById(1L)).thenReturn(Optional.of(testLeave));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(new Employee(2L, "Manager", "EMP002", null, null, EmployeeStatus.ACTIVE, LocalDate.now())));
        when(leaveRepository.save(any(Leave.class))).thenReturn(testLeave);

        Leave result = leaveService.denyLeave(1L, 2L, "Insufficient balance");

        assertNotNull(result);
        assertEquals(LeaveStatus.DENIED, result.getStatus());
    }

    @Test
    void testGetLeaveBalance_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.calculateBalance(1L)).thenReturn(10.0);

        double balance = leaveService.getLeaveBalance(1L);

        assertEquals(10.0, balance);
    }

    @Test
    void testCalculateAccrual_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.calculateAccrual(1L)).thenReturn(1.5);

        double accrual = leaveService.calculateAccrual(1L);

        assertEquals(1.5, accrual);
    }

    @Test
    void testRequestLeave_InvalidEmployee_ThrowsException() {
        leaveRequestDto.setEmployeeId(999L);
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> leaveService.requestLeave(leaveRequestDto));
    }

    @Test
    void testRequestLeave_OverlappingDates_ThrowsException() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(leaveRepository.existsOverlappingLeave(anyLong(), any(), any())).thenReturn(true);
        assertThrows(LeaveOverlapException.class, () -> leaveService.requestLeave(leaveRequestDto));
    }
}
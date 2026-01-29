@SpringBootTest
public class EmployeeServiceTest {
    @MockBean private EmployeeRepository employeeRepository;
    @MockBean private DepartmentRepository departmentRepository;
    @MockBean private RoleRepository roleRepository;
    @Autowired private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeDto testDto;
    private Department testDepartment;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testDepartment = new Department(1L, "Warehouse");
        testRole = new Role(1L, "WORKER");
        testEmployee = new Employee(1L, "John Doe", "EMP001", testDepartment, Set.of(testRole), EmployeeStatus.ACTIVE, LocalDate.now());
        testDto = new EmployeeDto("John Doe", "EMP001", 1L, Set.of(1L));
    }

    @Test
    void testCreateEmployee_ValidInput_Success() {
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        Employee result = employeeService.createEmployee(testDto);

        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testCreateEmployee_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(null));
    }

    @Test
    void testCreateEmployee_EmptyBadgeId_ThrowsException() {
        testDto.setBadgeId("");
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(testDto));
    }

    @Test
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() {
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(true);
        assertThrows(DuplicateBadgeIdException.class, () -> employeeService.createEmployee(testDto));
    }

    @Test
    void testCreateEmployee_InvalidDepartmentId_ThrowsException() {
        when(employeeRepository.existsByBadgeId("EMP001")).thenReturn(false);
        when(departmentRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.createEmployee(testDto));
    }

    @Test
    void testGetEmployees_Filtered_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        EmployeeFilter filter = new EmployeeFilter();
        Page<Employee> page = new PageImpl<>(List.of(testEmployee));
        when(employeeRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        Page<Employee> result = employeeService.getEmployees(filter, pageable);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void testGetEmployeeById_ValidId_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        Employee result = employeeService.getEmployeeById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void testGetEmployeeById_InvalidId_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.getEmployeeById(999L));
    }

    @Test
    void testUpdateEmployee_ValidInput_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(departmentRepository.findById(1L)).thenReturn(Optional.of(testDepartment));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(testRole));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        Employee result = employeeService.updateEmployee(1L, testDto);

        assertNotNull(result);
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    void testUpdateEmployee_NullDto_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(1L, null));
    }

    @Test
    void testSoftDeleteEmployee_ValidId_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);

        employeeService.softDeleteEmployee(1L);

        verify(employeeRepository).save(argThat(emp -> emp.getStatus() == EmployeeStatus.INACTIVE));
    }

    @Test
    void testSoftDeleteEmployee_InvalidId_ThrowsException() {
        when(employeeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.softDeleteEmployee(999L));
    }

    @Test
    void testFindByBadgeId_Valid_Success() {
        when(employeeRepository.findByBadgeId("EMP001")).thenReturn(Optional.of(testEmployee));
        Employee result = employeeService.findByBadgeId("EMP001");
        assertNotNull(result);
    }

    @Test
    void testFindByBadgeId_NotFound_ThrowsException() {
        when(employeeRepository.findByBadgeId("BADGE_X")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> employeeService.findByBadgeId("BADGE_X"));
    }
}
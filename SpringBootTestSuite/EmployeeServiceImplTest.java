public class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private EmployeeMapper employeeMapper;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("Should create employee successfully with valid input")
    void testCreate_ValidEmployee_ReturnsCreatedEmployee() {
        // Arrange
        EmployeeDTO inputDTO = EmployeeDTO.builder()
            .name("John Doe")
            .badgeId("EMP001")
            .role(Role.WORKER)
            .status(EmployeeStatus.ACTIVE)
            .build();

        Employee entity = new Employee();
        entity.setId(1L);
        entity.setName("John Doe");
        entity.setBadgeId("EMP001");

        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(false);
        when(employeeMapper.toEntity(inputDTO)).thenReturn(entity);
        when(employeeRepository.save(any(Employee.class))).thenReturn(entity);
        when(employeeMapper.toDTO(entity)).thenReturn(inputDTO);

        // Act
        EmployeeDTO result = employeeService.create(inputDTO);

        // Assert
        assertNotNull(result);
        assertEquals("John Doe", result.getName());
        assertEquals("EMP001", result.getBadgeId());
        verify(employeeRepository).existsByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Should throw BusinessException when badge ID already exists")
    void testCreate_DuplicateBadgeId_ThrowsBusinessException() {
        // Arrange
        EmployeeDTO inputDTO = EmployeeDTO.builder()
            .badgeId("EMP001")
            .build();

        when(employeeRepository.existsByBadgeIdAndDeletedFalse("EMP001")).thenReturn(true);

        // Act & Assert
        assertThrows(BusinessException.class, () -> employeeService.create(inputDTO));
        verify(employeeRepository).existsByBadgeIdAndDeletedFalse("EMP001");
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    // Additional test cases for other methods and edge cases would follow here...
}
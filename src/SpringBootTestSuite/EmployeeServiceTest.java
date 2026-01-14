package SpringBootTestSuite;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.*;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;

/**
 * Comprehensive JUnit tests for EmployeeService covering normal, boundary, and edge cases.
 * Covers: createEmployee, updateEmployee, deleteEmployee, getEmployee, listEmployees, listEmployeesByDepartment, listEmployeesByStatus.
 * Validates: unique badgeId, null/empty fields, soft delete, pagination, filtering, exception scenarios.
 *
 * @author AI
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;
    @InjectMocks
    private EmployeeService employeeService;

    private Employee testEmployee;
    private EmployeeRequestDTO testRequestDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testEmployee = Employee.builder()
                .id(1L)
                .badgeId("B12345")
                .name("John Doe")
                .role("WORKER")
                .department("Receiving")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusDays(10))
                .status(EmployeeStatus.ACTIVE)
                .deleted(false)
                .build();
        testRequestDTO = EmployeeRequestDTO.builder()
                .badgeId("B12345")
                .name("John Doe")
                .role("WORKER")
                .department("Receiving")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusDays(10))
                .status(EmployeeStatus.ACTIVE)
                .build();
        pageable = PageRequest.of(0, 10);
    }

    @Test
    @DisplayName("createEmployee with valid data returns created employee")
    void testCreateEmployee_WithValidData_ReturnsCreatedEmployee() {
        when(employeeRepository.existsByBadgeId("B12345")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(testEmployee);
        EmployeeResponseDTO result = employeeService.createEmployee(testRequestDTO);
        assertThat(result.getBadgeId()).isEqualTo("B12345");
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("createEmployee with duplicate badgeId throws BusinessException")
    void testCreateEmployee_WithDuplicateBadgeId_ThrowsBusinessException() {
        when(employeeRepository.existsByBadgeId("B12345")).thenReturn(true);
        assertThatThrownBy(() -> employeeService.createEmployee(testRequestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("badgeId must be unique");
    }

    @Test
    @DisplayName("createEmployee with null name throws validation exception")
    void testCreateEmployee_WithNullName_ThrowsValidationException() {
        testRequestDTO.setName(null);
        assertThatThrownBy(() -> employeeService.createEmployee(testRequestDTO))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("createEmployee with future hireDate throws validation exception")
    void testCreateEmployee_WithFutureHireDate_ThrowsValidationException() {
        testRequestDTO.setHireDate(LocalDate.now().plusDays(1));
        assertThatThrownBy(() -> employeeService.createEmployee(testRequestDTO))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @Test
    @DisplayName("updateEmployee with changed badgeId checks uniqueness")
    void testUpdateEmployee_WithChangedBadgeId_ChecksUniqueness() {
        Employee existing = Employee.builder().id(1L).badgeId("B12345").build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByBadgeId("B99999")).thenReturn(true);
        testRequestDTO.setBadgeId("B99999");
        assertThatThrownBy(() -> employeeService.updateEmployee(1L, testRequestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("badgeId must be unique");
    }

    @Test
    @DisplayName("updateEmployee with not found id throws ResourceNotFoundException")
    void testUpdateEmployee_WithNotFoundId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.updateEmployee(99L, testRequestDTO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("deleteEmployee performs soft delete")
    void testDeleteEmployee_PerformsSoftDelete() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        employeeService.deleteEmployee(1L);
        assertThat(testEmployee.isDeleted()).isTrue();
        verify(employeeRepository).save(testEmployee);
    }

    @Test
    @DisplayName("deleteEmployee with not found id throws ResourceNotFoundException")
    void testDeleteEmployee_WithNotFoundId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.deleteEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("getEmployee returns employee by id")
    void testGetEmployee_ReturnsEmployeeById() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        EmployeeResponseDTO result = employeeService.getEmployee(1L);
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("getEmployee with not found id throws ResourceNotFoundException")
    void testGetEmployee_WithNotFoundId_ThrowsResourceNotFoundException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> employeeService.getEmployee(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    @DisplayName("listEmployees returns paginated list")
    void testListEmployees_ReturnsPaginatedList() {
        Page<Employee> page = new PageImpl<>(List.of(testEmployee));
        when(employeeRepository.findAll(pageable)).thenReturn(page);
        Page<EmployeeResponseDTO> result = employeeService.listEmployees(pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("listEmployeesByDepartment filters by department")
    void testListEmployeesByDepartment_FiltersByDepartment() {
        Page<Employee> page = new PageImpl<>(List.of(testEmployee));
        when(employeeRepository.findByDepartmentAndDeletedFalse("Receiving", pageable)).thenReturn(page);
        Page<EmployeeResponseDTO> result = employeeService.listEmployeesByDepartment("Receiving", pageable);
        assertThat(result.getContent().get(0).getDepartment()).isEqualTo("Receiving");
    }

    @Test
    @DisplayName("listEmployeesByStatus filters by status")
    void testListEmployeesByStatus_FiltersByStatus() {
        Page<Employee> page = new PageImpl<>(List.of(testEmployee));
        when(employeeRepository.findByStatusAndDeletedFalse(EmployeeStatus.ACTIVE, pageable)).thenReturn(page);
        Page<EmployeeResponseDTO> result = employeeService.listEmployeesByStatus(EmployeeStatus.ACTIVE, pageable);
        assertThat(result.getContent().get(0).getStatus()).isEqualTo(EmployeeStatus.ACTIVE);
    }

    @AfterEach
    void tearDown() {
        // Clean up if needed
    }
}

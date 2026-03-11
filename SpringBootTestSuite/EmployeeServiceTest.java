package SpringBootTestSuite;

import com.warehouse.modules.employee.dto.EmployeeDto;
import com.warehouse.modules.employee.entity.Employee;
import com.warehouse.modules.employee.enums.EmployeeRole;
import com.warehouse.modules.employee.enums.EmployeeStatus;
import com.warehouse.modules.employee.repository.EmployeeRepository;
import com.warehouse.modules.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private EmployeeService employeeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    // Minimal stubs for missing classes/enums
    enum EmployeeRole { ADMIN, HR, SUPERVISOR, WORKER }
    enum EmployeeStatus { ACTIVE, TERMINATED }
    static class EmployeeDto {
        Long id;
        String badgeId;
        String name;
        EmployeeRole role;
        String department;
        String shiftGroup;
        LocalDate hireDate;
        EmployeeStatus status;
        // builder pattern for test convenience
        public static Builder builder() { return new Builder(); }
        static class Builder {
            EmployeeDto dto = new EmployeeDto();
            public Builder id(Long id) { dto.id = id; return this; }
            public Builder badgeId(String b) { dto.badgeId = b; return this; }
            public Builder name(String n) { dto.name = n; return this; }
            public Builder role(EmployeeRole r) { dto.role = r; return this; }
            public Builder department(String d) { dto.department = d; return this; }
            public Builder shiftGroup(String s) { dto.shiftGroup = s; return this; }
            public Builder hireDate(LocalDate h) { dto.hireDate = h; return this; }
            public Builder status(EmployeeStatus s) { dto.status = s; return this; }
            public EmployeeDto build() { return dto; }
        }
        // getters
        public Long getId() { return id; }
        public String getBadgeId() { return badgeId; }
        public String getName() { return name; }
        public EmployeeRole getRole() { return role; }
        public String getDepartment() { return department; }
        public String getShiftGroup() { return shiftGroup; }
        public LocalDate getHireDate() { return hireDate; }
        public EmployeeStatus getStatus() { return status; }
    }

    Employee employee = Employee.builder()
            .id(1L)
            .badgeId("B123")
            .name("John Doe")
            .role(EmployeeRole.ADMIN)
            .department("HR")
            .shiftGroup("A")
            .hireDate(LocalDate.of(2020,1,1))
            .status(EmployeeStatus.ACTIVE)
            .password("encoded")
            .deleted(false)
            .build();

    EmployeeDto employeeDto = EmployeeDto.builder()
            .id(1L)
            .badgeId("B123")
            .name("John Doe")
            .role(EmployeeRole.ADMIN)
            .department("HR")
            .shiftGroup("A")
            .hireDate(LocalDate.of(2020,1,1))
            .status(EmployeeStatus.ACTIVE)
            .build();

    @Test
    @DisplayName("getAllEmployees returns page of EmployeeDto")
    void getAllEmployees_normal() {
        Page<Employee> page = new PageImpl<>(List.of(employee));
        when(employeeRepository.findAllByDeletedFalse(any())).thenReturn(page);
        Page<EmployeeDto> result = employeeService.getAllEmployees(0, 10);
        assertEquals(1, result.getTotalElements());
        assertEquals("B123", result.getContent().get(0).getBadgeId());
    }

    @Test
    @DisplayName("getEmployeeById returns EmployeeDto if found and not deleted")
    void getEmployeeById_found() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        Optional<EmployeeDto> result = employeeService.getEmployeeById(1L);
        assertTrue(result.isPresent());
        assertEquals("B123", result.get().getBadgeId());
    }

    @Test
    @DisplayName("getEmployeeById returns empty if deleted")
    void getEmployeeById_deleted() {
        Employee deletedEmp = Employee.builder().id(2L).deleted(true).build();
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(deletedEmp));
        Optional<EmployeeDto> result = employeeService.getEmployeeById(2L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("getEmployeeById returns empty if not found")
    void getEmployeeById_notFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<EmployeeDto> result = employeeService.getEmployeeById(99L);
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("createEmployee creates and returns EmployeeDto")
    void createEmployee_normal() {
        when(employeeRepository.existsByBadgeId("B123")).thenReturn(false);
        when(passwordEncoder.encode("pass")).thenReturn("encoded");
        when(employeeRepository.save(any())).thenAnswer(inv -> {
            Employee e = inv.getArgument(0);
            e.setId(1L);
            return e;
        });
        EmployeeDto dto = EmployeeDto.builder()
                .badgeId("B123").name("John Doe").role(EmployeeRole.ADMIN)
                .department("HR").shiftGroup("A").hireDate(LocalDate.now()).build();
        EmployeeDto result = employeeService.createEmployee(dto, "pass");
        assertEquals("B123", result.getBadgeId());
        assertEquals(EmployeeStatus.ACTIVE, result.getStatus());
    }

    @Test
    @DisplayName("createEmployee throws if badgeId exists")
    void createEmployee_badgeIdExists() {
        when(employeeRepository.existsByBadgeId("B123")).thenReturn(true);
        EmployeeDto dto = EmployeeDto.builder().badgeId("B123").build();
        assertThrows(IllegalArgumentException.class, () -> employeeService.createEmployee(dto, "pass"));
    }

    @Test
    @DisplayName("updateEmployee updates and returns EmployeeDto")
    void updateEmployee_normal() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        EmployeeDto dto = EmployeeDto.builder()
                .name("Jane Doe").role(EmployeeRole.HR)
                .department("Finance").shiftGroup("B")
                .hireDate(LocalDate.of(2021,1,1)).status(EmployeeStatus.ACTIVE).build();
        EmployeeDto result = employeeService.updateEmployee(1L, dto);
        assertEquals("Jane Doe", result.getName());
        assertEquals(EmployeeRole.HR, result.getRole());
    }

    @Test
    @DisplayName("updateEmployee throws if not found")
    void updateEmployee_notFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        EmployeeDto dto = EmployeeDto.builder().build();
        assertThrows(IllegalArgumentException.class, () -> employeeService.updateEmployee(99L, dto));
    }

    @Test
    @DisplayName("softDeleteEmployee sets deleted and status")
    void softDeleteEmployee_normal() {
        Employee emp = Employee.builder().id(1L).deleted(false).status(EmployeeStatus.ACTIVE).build();
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(emp));
        when(employeeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        employeeService.softDeleteEmployee(1L);
        assertTrue(emp.isDeleted());
        assertEquals(EmployeeStatus.TERMINATED, emp.getStatus());
    }

    @Test
    @DisplayName("softDeleteEmployee throws if not found")
    void softDeleteEmployee_notFound() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> employeeService.softDeleteEmployee(99L));
    }
}

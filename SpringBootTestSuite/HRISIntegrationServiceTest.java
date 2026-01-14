package com.warehouse.ems.integration;

import com.warehouse.ems.employee.Employee;
import com.warehouse.ems.employee.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit test suite for HRISIntegrationService
 * Tests cover HRIS synchronization, retry mechanisms, error handling, and edge cases
 */
@ExtendWith(MockitoExtension.class)
public class HRISIntegrationServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private EmployeeService employeeService;

    @Mock
    private WebhookEventPublisher webhookEventPublisher;

    @InjectMocks
    private HRISIntegrationService hrisIntegrationService;

    private Employee testEmployee;
    private HRISEmployeeDTO hrisEmployeeDTO;

    @BeforeEach
    public void setUp() {
        // Setup test employee
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setFirstName("John");
        testEmployee.setLastName("Doe");
        testEmployee.setEmail("john.doe@warehouse.com");
        testEmployee.setPhone("+1234567890");
        testEmployee.setDepartment("Warehouse");
        testEmployee.setRole("WORKER");
        testEmployee.setHireDate(LocalDate.now());
        testEmployee.setStatus("ACTIVE");
        testEmployee.setTenantId("TENANT001");

        // Setup HRIS DTO
        hrisEmployeeDTO = new HRISEmployeeDTO();
        hrisEmployeeDTO.setEmployeeId("EMP001");
        hrisEmployeeDTO.setFirstName("John");
        hrisEmployeeDTO.setLastName("Doe");
        hrisEmployeeDTO.setEmail("john.doe@warehouse.com");
        hrisEmployeeDTO.setPhone("+1234567890");
        hrisEmployeeDTO.setDepartment("Warehouse");
        hrisEmployeeDTO.setPosition("WORKER");
        hrisEmployeeDTO.setHireDate(LocalDate.now());
        hrisEmployeeDTO.setStatus("ACTIVE");
    }

    // ========== SYNC EMPLOYEES TESTS ==========

    @Test
    public void testSyncEmployees_NewEmployees_CreatesEmployees() {
        // Arrange
        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(null);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(employeeService, times(1)).createEmployee(any(Employee.class));
        verify(webhookEventPublisher, times(1)).publishEvent(eq("employee.created"), any());
    }

    @Test
    public void testSyncEmployees_ExistingEmployees_UpdatesEmployees() {
        // Arrange
        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(testEmployee);
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(employeeService, times(1)).updateEmployee(anyLong(), any(Employee.class));
        verify(webhookEventPublisher, times(1)).publishEvent(eq("employee.updated"), any());
    }

    @Test
    public void testSyncEmployees_EmptyResponse_NoAction() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(new HRISEmployeeDTO[0]);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(employeeService, never()).createEmployee(any(Employee.class));
        verify(employeeService, never()).updateEmployee(anyLong(), any(Employee.class));
    }

    @Test
    public void testSyncEmployees_NullResponse_NoAction() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(null);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(employeeService, never()).createEmployee(any(Employee.class));
        verify(employeeService, never()).updateEmployee(anyLong(), any(Employee.class));
    }

    @Test
    public void testSyncEmployees_MultipleEmployees_ProcessesAll() {
        // Arrange
        HRISEmployeeDTO employee2 = new HRISEmployeeDTO();
        employee2.setEmployeeId("EMP002");
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");
        employee2.setEmail("jane.smith@warehouse.com");

        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO, employee2);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId(anyString())).thenReturn(null);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(employeeService, times(2)).createEmployee(any(Employee.class));
        verify(webhookEventPublisher, times(2)).publishEvent(eq("employee.created"), any());
    }

    // ========== ERROR HANDLING TESTS ==========

    @Test
    public void testSyncEmployees_RestClientException_HandlesGracefully() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenThrow(new RestClientException("Connection failed"));

        // Act & Assert
        assertDoesNotThrow(() -> hrisIntegrationService.syncEmployees());
        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    public void testSyncEmployees_ServiceException_ContinuesProcessing() {
        // Arrange
        HRISEmployeeDTO employee2 = new HRISEmployeeDTO();
        employee2.setEmployeeId("EMP002");
        employee2.setFirstName("Jane");
        employee2.setLastName("Smith");

        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO, employee2);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(null);
        when(employeeService.getEmployeeByBadgeId("EMP002")).thenReturn(null);
        when(employeeService.createEmployee(any(Employee.class)))
                .thenThrow(new RuntimeException("Database error"))
                .thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert - Should continue processing despite first failure
        verify(employeeService, times(2)).createEmployee(any(Employee.class));
    }

    // ========== RETRY MECHANISM TESTS ==========

    @Test
    public void testSyncEmployees_RetryOnFailure_Success() {
        // Arrange
        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenThrow(new RestClientException("Temporary failure"))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(null);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployeesWithRetry();

        // Assert
        verify(restTemplate, times(2)).getForObject(anyString(), eq(HRISEmployeeDTO[].class));
        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    public void testSyncEmployees_MaxRetriesExceeded_ThrowsException() {
        // Arrange
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenThrow(new RestClientException("Persistent failure"));

        // Act & Assert
        assertThrows(RuntimeException.class, () -> {
            hrisIntegrationService.syncEmployeesWithRetry();
        });

        verify(restTemplate, times(3)).getForObject(anyString(), eq(HRISEmployeeDTO[].class));
    }

    // ========== DATA MAPPING TESTS ==========

    @Test
    public void testMapHRISToEmployee_ValidData_Success() {
        // Act
        Employee mappedEmployee = hrisIntegrationService.mapHRISToEmployee(hrisEmployeeDTO);

        // Assert
        assertNotNull(mappedEmployee);
        assertEquals("EMP001", mappedEmployee.getBadgeId());
        assertEquals("John", mappedEmployee.getFirstName());
        assertEquals("Doe", mappedEmployee.getLastName());
        assertEquals("john.doe@warehouse.com", mappedEmployee.getEmail());
    }

    @Test
    public void testMapHRISToEmployee_NullDTO_ThrowsException() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            hrisIntegrationService.mapHRISToEmployee(null);
        });
    }

    @Test
    public void testMapHRISToEmployee_MissingRequiredFields_ThrowsException() {
        // Arrange
        hrisEmployeeDTO.setEmployeeId(null);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            hrisIntegrationService.mapHRISToEmployee(hrisEmployeeDTO);
        });
    }

    @Test
    public void testMapHRISToEmployee_PartialData_Success() {
        // Arrange
        hrisEmployeeDTO.setPhone(null);
        hrisEmployeeDTO.setDepartment(null);

        // Act
        Employee mappedEmployee = hrisIntegrationService.mapHRISToEmployee(hrisEmployeeDTO);

        // Assert
        assertNotNull(mappedEmployee);
        assertNull(mappedEmployee.getPhone());
        assertNull(mappedEmployee.getDepartment());
    }

    // ========== TERMINATION HANDLING TESTS ==========

    @Test
    public void testSyncEmployees_TerminatedEmployee_UpdatesStatus() {
        // Arrange
        hrisEmployeeDTO.setStatus("TERMINATED");
        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(testEmployee);
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(employeeService, times(1)).updateEmployee(anyLong(), any(Employee.class));
        verify(webhookEventPublisher, times(1)).publishEvent(eq("employee.terminated"), any());
    }

    @Test
    public void testSyncEmployees_TerminatedEmployee_TriggersOffboarding() {
        // Arrange
        hrisEmployeeDTO.setStatus("TERMINATED");
        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(testEmployee);
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(webhookEventPublisher, times(1)).publishEvent(eq("employee.terminated"), any());
    }

    // ========== IDEMPOTENCY TESTS ==========

    @Test
    public void testSyncEmployees_DuplicateSync_Idempotent() {
        // Arrange
        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(testEmployee);
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(testEmployee);

        // Act - Sync twice
        hrisIntegrationService.syncEmployees();
        hrisIntegrationService.syncEmployees();

        // Assert - Should update twice but not create duplicates
        verify(employeeService, times(2)).updateEmployee(anyLong(), any(Employee.class));
        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    // ========== WEBHOOK EVENT TESTS ==========

    @Test
    public void testSyncEmployees_NewEmployee_PublishesCreatedEvent() {
        // Arrange
        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(null);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(webhookEventPublisher, times(1)).publishEvent(eq("employee.created"), any());
    }

    @Test
    public void testSyncEmployees_UpdatedEmployee_PublishesUpdatedEvent() {
        // Arrange
        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(testEmployee);
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(webhookEventPublisher, times(1)).publishEvent(eq("employee.updated"), any());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    public void testSyncEmployees_LargeDataset_ProcessesAll() {
        // Arrange - Create 1000 employees
        HRISEmployeeDTO[] largeDataset = new HRISEmployeeDTO[1000];
        for (int i = 0; i < 1000; i++) {
            HRISEmployeeDTO dto = new HRISEmployeeDTO();
            dto.setEmployeeId("EMP" + String.format("%04d", i));
            dto.setFirstName("Employee" + i);
            dto.setLastName("Test");
            dto.setEmail("employee" + i + "@test.com");
            largeDataset[i] = dto;
        }

        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(largeDataset);
        when(employeeService.getEmployeeByBadgeId(anyString())).thenReturn(null);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(employeeService, times(1000)).createEmployee(any(Employee.class));
    }

    @Test
    public void testSyncEmployees_SpecialCharactersInData_HandlesCorrectly() {
        // Arrange
        hrisEmployeeDTO.setFirstName("Jean-Pierre");
        hrisEmployeeDTO.setLastName("O'Connor");
        List<HRISEmployeeDTO> hrisEmployees = Arrays.asList(hrisEmployeeDTO);
        when(restTemplate.getForObject(anyString(), eq(HRISEmployeeDTO[].class)))
                .thenReturn(hrisEmployees.toArray(new HRISEmployeeDTO[0]));
        when(employeeService.getEmployeeByBadgeId("EMP001")).thenReturn(null);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act
        hrisIntegrationService.syncEmployees();

        // Assert
        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    // Helper DTO class
    private static class HRISEmployeeDTO {
        private String employeeId;
        private String firstName;
        private String lastName;
        private String email;
        private String phone;
        private String department;
        private String position;
        private LocalDate hireDate;
        private String status;

        // Getters and setters
        public String getEmployeeId() { return employeeId; }
        public void setEmployeeId(String employeeId) { this.employeeId = employeeId; }
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        public String getPhone() { return phone; }
        public void setPhone(String phone) { this.phone = phone; }
        public String getDepartment() { return department; }
        public void setDepartment(String department) { this.department = department; }
        public String getPosition() { return position; }
        public void setPosition(String position) { this.position = position; }
        public LocalDate getHireDate() { return hireDate; }
        public void setHireDate(LocalDate hireDate) { this.hireDate = hireDate; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
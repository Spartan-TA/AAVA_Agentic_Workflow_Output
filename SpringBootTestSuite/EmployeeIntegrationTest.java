package com.warehouse.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.dto.EmployeeDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive Integration Test Suite for Employee Module
 * Tests cover end-to-end scenarios with real database interactions
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    private EmployeeDTO validEmployeeDTO;

    @BeforeEach
    public void setUp() {
        // Clean up database before each test
        employeeRepository.deleteAll();

        // Arrange: Set up valid test data
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setBadgeId("EMP001");
        validEmployeeDTO.setFirstName("John");
        validEmployeeDTO.setLastName("Doe");
        validEmployeeDTO.setEmail("john.doe@warehouse.com");
        validEmployeeDTO.setRole(EmployeeRole.WORKER);
        validEmployeeDTO.setDepartment("Warehouse");
        validEmployeeDTO.setShiftGroup("Morning");
        validEmployeeDTO.setHireDate(LocalDate.now());
        validEmployeeDTO.setStatus(EmployeeStatus.ACTIVE);
    }

    // ========== CREATE EMPLOYEE INTEGRATION TESTS ==========

    @Test
    public void testCreateEmployee_EndToEnd_ShouldPersistToDatabase() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.badgeId", is("EMP001")))
                .andExpect(jsonPath("$.firstName", is("John")))
                .andExpect(jsonPath("$.lastName", is("Doe")));

        // Verify database persistence
        assert employeeRepository.findByBadgeId("EMP001").isPresent();
    }

    @Test
    public void testCreateEmployee_WithDuplicateBadgeId_ShouldFailWithConflict() throws Exception {
        // Arrange: Create first employee
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated());

        // Act & Assert: Try to create duplicate
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message", containsString("Badge ID")));
    }

    // ========== READ EMPLOYEE INTEGRATION TESTS ==========

    @Test
    public void testGetEmployeeById_EndToEnd_ShouldRetrieveFromDatabase() throws Exception {
        // Arrange: Create employee
        String createResponse = mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDTO createdEmployee = objectMapper.readValue(createResponse, EmployeeDTO.class);

        // Act & Assert: Retrieve employee
        mockMvc.perform(get("/api/employees/" + createdEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdEmployee.getId().intValue())))
                .andExpect(jsonPath("$.badgeId", is("EMP001")));
    }

    @Test
    public void testGetAllEmployees_EndToEnd_ShouldReturnPaginatedResults() throws Exception {
        // Arrange: Create multiple employees
        for (int i = 1; i <= 5; i++) {
            EmployeeDTO dto = new EmployeeDTO();
            dto.setBadgeId("EMP" + String.format("%03d", i));
            dto.setFirstName("Employee" + i);
            dto.setLastName("Test");
            dto.setEmail("employee" + i + "@warehouse.com");
            dto.setRole(EmployeeRole.WORKER);
            dto.setDepartment("Warehouse");
            dto.setShiftGroup("Morning");
            dto.setHireDate(LocalDate.now());
            dto.setStatus(EmployeeStatus.ACTIVE);

            mockMvc.perform(post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        // Act & Assert: Get all employees
        mockMvc.perform(get("/api/employees?page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.totalElements", is(5)));
    }

    // ========== UPDATE EMPLOYEE INTEGRATION TESTS ==========

    @Test
    public void testUpdateEmployee_EndToEnd_ShouldPersistChanges() throws Exception {
        // Arrange: Create employee
        String createResponse = mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDTO createdEmployee = objectMapper.readValue(createResponse, EmployeeDTO.class);

        // Modify employee data
        createdEmployee.setFirstName("Jane");
        createdEmployee.setDepartment("Logistics");

        // Act & Assert: Update employee
        mockMvc.perform(put("/api/employees/" + createdEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createdEmployee)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName", is("Jane")))
                .andExpect(jsonPath("$.department", is("Logistics")));

        // Verify database update
        Employee updatedEmployee = employeeRepository.findById(createdEmployee.getId()).orElseThrow();
        assert updatedEmployee.getFirstName().equals("Jane");
        assert updatedEmployee.getDepartment().equals("Logistics");
    }

    // ========== DELETE EMPLOYEE INTEGRATION TESTS ==========

    @Test
    public void testDeleteEmployee_EndToEnd_ShouldSoftDelete() throws Exception {
        // Arrange: Create employee
        String createResponse = mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDTO createdEmployee = objectMapper.readValue(createResponse, EmployeeDTO.class);

        // Act: Delete employee
        mockMvc.perform(delete("/api/employees/" + createdEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Assert: Verify soft delete
        Employee deletedEmployee = employeeRepository.findById(createdEmployee.getId()).orElseThrow();
        assert deletedEmployee.getDeleted();

        // Verify employee is not in active list
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    public void testRestoreEmployee_EndToEnd_ShouldRestoreDeletedEmployee() throws Exception {
        // Arrange: Create and delete employee
        String createResponse = mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDTO createdEmployee = objectMapper.readValue(createResponse, EmployeeDTO.class);

        mockMvc.perform(delete("/api/employees/" + createdEmployee.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        // Act: Restore employee
        mockMvc.perform(post("/api/employees/" + createdEmployee.getId() + "/restore")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(createdEmployee.getId().intValue())));

        // Assert: Verify restoration
        Employee restoredEmployee = employeeRepository.findById(createdEmployee.getId()).orElseThrow();
        assert !restoredEmployee.getDeleted();

        // Verify employee is back in active list
        mockMvc.perform(get("/api/employees")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }

    // ========== SEARCH AND FILTER INTEGRATION TESTS ==========

    @Test
    public void testSearchByDepartment_EndToEnd_ShouldFilterResults() throws Exception {
        // Arrange: Create employees in different departments
        EmployeeDTO warehouseEmployee = createEmployeeDTO("EMP001", "John", "Doe", "Warehouse");
        EmployeeDTO logisticsEmployee = createEmployeeDTO("EMP002", "Jane", "Smith", "Logistics");

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(warehouseEmployee)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logisticsEmployee)))
                .andExpect(status().isCreated());

        // Act & Assert: Filter by department
        mockMvc.perform(get("/api/employees?department=Warehouse")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].department", is("Warehouse")));
    }

    @Test
    public void testSearchByRole_EndToEnd_ShouldFilterResults() throws Exception {
        // Arrange: Create employees with different roles
        EmployeeDTO worker = createEmployeeDTOWithRole("EMP001", "John", "Doe", EmployeeRole.WORKER);
        EmployeeDTO supervisor = createEmployeeDTOWithRole("EMP002", "Jane", "Smith", EmployeeRole.SUPERVISOR);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(worker)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(supervisor)))
                .andExpect(status().isCreated());

        // Act & Assert: Filter by role
        mockMvc.perform(get("/api/employees?role=SUPERVISOR")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].role", is("SUPERVISOR")));
    }

    // ========== PAGINATION INTEGRATION TESTS ==========

    @Test
    public void testPagination_EndToEnd_ShouldReturnCorrectPages() throws Exception {
        // Arrange: Create 25 employees
        for (int i = 1; i <= 25; i++) {
            EmployeeDTO dto = createEmployeeDTO(
                "EMP" + String.format("%03d", i),
                "Employee" + i,
                "Test",
                "Warehouse"
            );

            mockMvc.perform(post("/api/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        // Act & Assert: Test first page
        mockMvc.perform(get("/api/employees?page=0&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.totalElements", is(25)))
                .andExpect(jsonPath("$.totalPages", is(3)))
                .andExpect(jsonPath("$.number", is(0)));

        // Test second page
        mockMvc.perform(get("/api/employees?page=1&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.number", is(1)));

        // Test last page
        mockMvc.perform(get("/api/employees?page=2&size=10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(5)))
                .andExpect(jsonPath("$.number", is(2)));
    }

    // ========== VALIDATION INTEGRATION TESTS ==========

    @Test
    public void testCreateEmployee_WithInvalidEmail_ShouldFailValidation() throws Exception {
        // Arrange
        validEmployeeDTO.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isBadRequest());

        // Verify no employee was created
        assert employeeRepository.count() == 0;
    }

    @Test
    public void testCreateEmployee_WithMissingRequiredFields_ShouldFailValidation() throws Exception {
        // Arrange
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setBadgeId("EMP001");
        // Missing firstName, lastName, email, etc.

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());

        // Verify no employee was created
        assert employeeRepository.count() == 0;
    }

    // ========== HELPER METHODS ==========

    private EmployeeDTO createEmployeeDTO(String badgeId, String firstName, String lastName, String department) {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId(badgeId);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@warehouse.com");
        dto.setRole(EmployeeRole.WORKER);
        dto.setDepartment(department);
        dto.setShiftGroup("Morning");
        dto.setHireDate(LocalDate.now());
        dto.setStatus(EmployeeStatus.ACTIVE);
        return dto;
    }

    private EmployeeDTO createEmployeeDTOWithRole(String badgeId, String firstName, String lastName, EmployeeRole role) {
        EmployeeDTO dto = createEmployeeDTO(badgeId, firstName, lastName, "Warehouse");
        dto.setRole(role);
        return dto;
    }
}
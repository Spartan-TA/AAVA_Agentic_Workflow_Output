package com.company.wms.employee;

import com.company.wms.employee.dto.EmployeeCreateDTO;
import com.company.wms.employee.dto.EmployeeDTO;
import com.company.wms.employee.entity.EmployeeRole;
import com.company.wms.employee.entity.EmployeeStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDate;

import static org.hamcrest.Matchers.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive Integration Test Suite for Employee Module
 * Uses Testcontainers for PostgreSQL database
 * Tests end-to-end functionality from REST API to database
 */
@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
class EmployeeIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("wms_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeCreateDTO testCreateDTO;

    @BeforeEach
    void setUp() {
        testCreateDTO = new EmployeeCreateDTO();
        testCreateDTO.setBadgeId("EMP" + System.currentTimeMillis()); // Unique badge ID
        testCreateDTO.setName("John Doe");
        testCreateDTO.setRole(EmployeeRole.WORKER);
        testCreateDTO.setDepartment("Warehouse");
        testCreateDTO.setShiftGroup("Day Shift");
        testCreateDTO.setHireDate(LocalDate.of(2024, 1, 1));
    }

    // ==================== FULL CRUD INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testFullCrudLifecycle_CreateReadUpdateDelete_Success() throws Exception {
        // 1. CREATE - Create new employee
        String createResponse = mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value(testCreateDTO.getBadgeId()))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDTO createdEmployee = objectMapper.readValue(createResponse, EmployeeDTO.class);
        Long employeeId = createdEmployee.getId();

        // 2. READ - Get employee by ID
        mockMvc.perform(get("/api/v1/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.badgeId").value(testCreateDTO.getBadgeId()))
                .andExpect(jsonPath("$.name").value("John Doe"));

        // 3. UPDATE - Update employee
        String updateJson = "{"name":"John Updated","department":"Logistics"}";
        mockMvc.perform(put("/api/v1/employees/" + employeeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId));

        // 4. VERIFY UPDATE - Read updated employee
        mockMvc.perform(get("/api/v1/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId));

        // 5. DELETE - Soft delete employee
        mockMvc.perform(delete("/api/v1/employees/" + employeeId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        // 6. VERIFY DELETE - Employee should not be found
        mockMvc.perform(get("/api/v1/employees/" + employeeId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_DatabasePersistence_Success() throws Exception {
        // Create employee
        String response = mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDTO created = objectMapper.readValue(response, EmployeeDTO.class);

        // Verify persistence by retrieving from database
        mockMvc.perform(get("/api/v1/employees/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value(testCreateDTO.getBadgeId()))
                .andExpect(jsonPath("$.name").value(testCreateDTO.getName()))
                .andExpect(jsonPath("$.role").value(testCreateDTO.getRole().toString()))
                .andExpect(jsonPath("$.department").value(testCreateDTO.getDepartment()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_DuplicateBadgeId_ReturnsConflict() throws Exception {
        // Create first employee
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isCreated());

        // Try to create duplicate
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("Badge ID already exists")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListEmployees_Pagination_Success() throws Exception {
        // Create multiple employees
        for (int i = 0; i < 5; i++) {
            EmployeeCreateDTO dto = new EmployeeCreateDTO();
            dto.setBadgeId("EMP" + System.currentTimeMillis() + i);
            dto.setName("Employee " + i);
            dto.setRole(EmployeeRole.WORKER);
            dto.setDepartment("Warehouse");
            dto.setHireDate(LocalDate.now());

            mockMvc.perform(post("/api/v1/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isCreated());
        }

        // Test pagination
        mockMvc.perform(get("/api/v1/employees")
                .param("page", "0")
                .param("size", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(3))))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(5)));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testListEmployees_FilterByDepartment_Success() throws Exception {
        // Create employees in different departments
        EmployeeCreateDTO warehouseEmp = new EmployeeCreateDTO();
        warehouseEmp.setBadgeId("WH" + System.currentTimeMillis());
        warehouseEmp.setName("Warehouse Worker");
        warehouseEmp.setRole(EmployeeRole.WORKER);
        warehouseEmp.setDepartment("Warehouse");
        warehouseEmp.setHireDate(LocalDate.now());

        EmployeeCreateDTO logisticsEmp = new EmployeeCreateDTO();
        logisticsEmp.setBadgeId("LOG" + System.currentTimeMillis());
        logisticsEmp.setName("Logistics Worker");
        logisticsEmp.setRole(EmployeeRole.WORKER);
        logisticsEmp.setDepartment("Logistics");
        logisticsEmp.setHireDate(LocalDate.now());

        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(warehouseEmp)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(logisticsEmp)))
                .andExpect(status().isCreated());

        // Filter by Warehouse department
        mockMvc.perform(get("/api/v1/employees")
                .param("department", "Warehouse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].department", everyItem(is("Warehouse"))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateEmployee_PartialUpdate_Success() throws Exception {
        // Create employee
        String createResponse = mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDTO created = objectMapper.readValue(createResponse, EmployeeDTO.class);

        // Partial update - only name
        String updateJson = "{"name":"Updated Name"}";
        mockMvc.perform(put("/api/v1/employees/" + created.getId())
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(updateJson))
                .andExpect(status().isOk());

        // Verify other fields unchanged
        mockMvc.perform(get("/api/v1/employees/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value(testCreateDTO.getDepartment()))
                .andExpect(jsonPath("$.role").value(testCreateDTO.getRole().toString()));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testDeleteEmployee_SoftDelete_Success() throws Exception {
        // Create employee
        String createResponse = mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        EmployeeDTO created = objectMapper.readValue(createResponse, EmployeeDTO.class);

        // Delete employee
        mockMvc.perform(delete("/api/v1/employees/" + created.getId())
                .with(csrf()))
                .andExpect(status().isNoContent());

        // Verify employee is not accessible
        mockMvc.perform(get("/api/v1/employees/" + created.getId()))
                .andExpect(status().isNotFound());

        // Verify employee is not in list
        mockMvc.perform(get("/api/v1/employees")
                .param("badgeId", testCreateDTO.getBadgeId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[?(@.badgeId == '" + testCreateDTO.getBadgeId() + "')]").doesNotExist());
    }

    // ==================== SECURITY INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "HR")
    void testCreateEmployee_AsHR_Success() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    void testCreateEmployee_AsSupervisor_Forbidden() throws Exception {
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    void testDeleteEmployee_AsWorker_Forbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());
    }

    // ==================== VALIDATION INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_InvalidData_ReturnsBadRequest() throws Exception {
        testCreateDTO.setBadgeId(""); // Empty badge ID
        testCreateDTO.setName(null); // Null name

        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_FutureHireDate_ReturnsBadRequest() throws Exception {
        testCreateDTO.setHireDate(LocalDate.now().plusDays(30));

        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isBadRequest());
    }

    // ==================== TRANSACTION INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    void testCreateEmployee_TransactionRollback_OnError() throws Exception {
        // This test verifies that if an error occurs, the transaction is rolled back
        // and no partial data is saved to the database
        
        testCreateDTO.setBadgeId("ROLLBACK_TEST");
        
        // Create employee successfully
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isCreated());

        // Try to create duplicate (should fail)
        mockMvc.perform(post("/api/v1/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testCreateDTO)))
                .andExpect(status().isConflict());

        // Verify only one employee exists with this badge ID
        mockMvc.perform(get("/api/v1/employees")
                .param("search", "ROLLBACK_TEST"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)));
    }
}

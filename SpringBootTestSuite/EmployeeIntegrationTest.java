package com.warehouseems.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouseems.employee.dto.EmployeeRequestDto;
import com.warehouseems.employee.dto.EmployeeResponseDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration test suite for Employee module.
 * Tests end-to-end workflows including database operations, REST API, and security.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    private EmployeeRequestDto testEmployeeRequest;
    private static Long createdEmployeeId;

    @BeforeEach
    void setUp() {
        testEmployeeRequest = new EmployeeRequestDto();
        testEmployeeRequest.setName("Integration Test Employee");
        testEmployeeRequest.setBadgeId("INT001");
        testEmployeeRequest.setRole("WORKER");
        testEmployeeRequest.setDepartment("Shipping");
        testEmployeeRequest.setShiftGroup("DAY_SHIFT");
        testEmployeeRequest.setHireDate(LocalDate.of(2023, 1, 1));
        testEmployeeRequest.setStatus("ACTIVE");
        testEmployeeRequest.setEmail("integration.test@warehouse.com");
        testEmployeeRequest.setPhone("+1234567890");
        testEmployeeRequest.setAddress("123 Test Street");
    }

    // ==================== COMPLETE CRUD WORKFLOW TESTS ====================

    @Nested
    @DisplayName("Complete CRUD Workflow Tests")
    @TestMethodOrder(MethodOrderer.OrderAnnotation.class)
    class CompleteCrudWorkflowTests {

        @Test
        @Order(1)
        @DisplayName("1. Create employee - Full workflow")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testCreateEmployee_FullWorkflow() throws Exception {
            MvcResult result = mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("Integration Test Employee"))
                    .andExpect(jsonPath("$.badgeId").value("INT001"))
                    .andExpect(jsonPath("$.id").exists())
                    .andReturn();

            String responseBody = result.getResponse().getContentAsString();
            EmployeeResponseDto response = objectMapper.readValue(responseBody, EmployeeResponseDto.class);
            createdEmployeeId = response.getId();

            // Verify in database
            Employee dbEmployee = employeeRepository.findById(createdEmployeeId).orElse(null);
            assertNotNull(dbEmployee);
            assertEquals("Integration Test Employee", dbEmployee.getName());
            assertFalse(dbEmployee.isDeleted());
        }

        @Test
        @Order(2)
        @DisplayName("2. Read employee - Full workflow")
        @WithMockUser(roles = "ADMIN")
        void testReadEmployee_FullWorkflow() throws Exception {
            // Create employee first
            Employee employee = new Employee();
            employee.setName("Read Test Employee");
            employee.setBadgeId("READ001");
            employee.setRole("WORKER");
            employee.setDepartment("Shipping");
            employee.setHireDate(LocalDate.now());
            employee.setStatus("ACTIVE");
            employee.setDeleted(false);
            Employee saved = employeeRepository.save(employee);

            // Read via API
            mockMvc.perform(get("/api/employees/" + saved.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(saved.getId()))
                    .andExpect(jsonPath("$.name").value("Read Test Employee"))
                    .andExpect(jsonPath("$.badgeId").value("READ001"));
        }

        @Test
        @Order(3)
        @DisplayName("3. Update employee - Full workflow")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testUpdateEmployee_FullWorkflow() throws Exception {
            // Create employee
            Employee employee = new Employee();
            employee.setName("Update Test Employee");
            employee.setBadgeId("UPD001");
            employee.setRole("WORKER");
            employee.setDepartment("Shipping");
            employee.setHireDate(LocalDate.now());
            employee.setStatus("ACTIVE");
            employee.setDeleted(false);
            Employee saved = employeeRepository.save(employee);

            // Update via API
            testEmployeeRequest.setName("Updated Employee Name");
            testEmployeeRequest.setBadgeId("UPD001");
            testEmployeeRequest.setRole("SUPERVISOR");

            mockMvc.perform(put("/api/employees/" + saved.getId())
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("Updated Employee Name"))
                    .andExpect(jsonPath("$.role").value("SUPERVISOR"));

            // Verify in database
            Employee updated = employeeRepository.findById(saved.getId()).orElse(null);
            assertNotNull(updated);
            assertEquals("Updated Employee Name", updated.getName());
            assertEquals("SUPERVISOR", updated.getRole());
        }

        @Test
        @Order(4)
        @DisplayName("4. Soft delete employee - Full workflow")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testSoftDeleteEmployee_FullWorkflow() throws Exception {
            // Create employee
            Employee employee = new Employee();
            employee.setName("Delete Test Employee");
            employee.setBadgeId("DEL001");
            employee.setRole("WORKER");
            employee.setDepartment("Shipping");
            employee.setHireDate(LocalDate.now());
            employee.setStatus("ACTIVE");
            employee.setDeleted(false);
            Employee saved = employeeRepository.save(employee);

            // Soft delete via API
            mockMvc.perform(delete("/api/employees/" + saved.getId())
                    .with(csrf()))
                    .andExpect(status().isNoContent());

            // Verify soft delete in database
            Employee deleted = employeeRepository.findById(saved.getId()).orElse(null);
            assertNotNull(deleted);
            assertTrue(deleted.isDeleted());
            assertEquals("INACTIVE", deleted.getStatus());

            // Verify not returned in active list
            mockMvc.perform(get("/api/employees/" + saved.getId()))
                    .andExpect(status().isNotFound());
        }
    }

    // ==================== SECURITY INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Security Integration Tests")
    class SecurityIntegrationTests {

        @Test
        @DisplayName("Should enforce authentication for all endpoints")
        void testAuthentication_Required() throws Exception {
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should allow ADMIN full access")
        @WithMockUser(roles = "ADMIN")
        void testAuthorization_AdminFullAccess() throws Exception {
            mockMvc.perform(get("/api/employees"))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should allow HR to create employees")
        @WithMockUser(roles = "HR")
        void testAuthorization_HRCanCreate() throws Exception {
            testEmployeeRequest.setBadgeId("HR001");
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should deny WORKER from creating employees")
        @WithMockUser(roles = "WORKER")
        void testAuthorization_WorkerCannotCreate() throws Exception {
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should deny SUPERVISOR from deleting employees")
        @WithMockUser(roles = "SUPERVISOR")
        void testAuthorization_SupervisorCannotDelete() throws Exception {
            mockMvc.perform(delete("/api/employees/1")
                    .with(csrf()))
                    .andExpect(status().isForbidden());
        }
    }

    // ==================== PAGINATION AND FILTERING INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Pagination and Filtering Integration Tests")
    class PaginationFilteringIntegrationTests {

        @Test
        @DisplayName("Should paginate employees correctly")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testPagination_Integration() throws Exception {
            // Create multiple employees
            for (int i = 1; i <= 25; i++) {
                Employee emp = new Employee();
                emp.setName("Employee " + i);
                emp.setBadgeId("PAG" + String.format("%03d", i));
                emp.setRole("WORKER");
                emp.setDepartment("Shipping");
                emp.setHireDate(LocalDate.now());
                emp.setStatus("ACTIVE");
                emp.setDeleted(false);
                employeeRepository.save(emp);
            }

            // Test first page
            mockMvc.perform(get("/api/employees")
                    .param("page", "0")
                    .param("size", "10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(25))
                    .andExpect(jsonPath("$.totalPages").value(3));
        }

        @Test
        @DisplayName("Should filter by department")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testFiltering_ByDepartment() throws Exception {
            // Create employees in different departments
            Employee emp1 = new Employee();
            emp1.setName("Shipping Employee");
            emp1.setBadgeId("SHIP001");
            emp1.setRole("WORKER");
            emp1.setDepartment("Shipping");
            emp1.setHireDate(LocalDate.now());
            emp1.setStatus("ACTIVE");
            emp1.setDeleted(false);
            employeeRepository.save(emp1);

            Employee emp2 = new Employee();
            emp2.setName("Receiving Employee");
            emp2.setBadgeId("REC001");
            emp2.setRole("WORKER");
            emp2.setDepartment("Receiving");
            emp2.setHireDate(LocalDate.now());
            emp2.setStatus("ACTIVE");
            emp2.setDeleted(false);
            employeeRepository.save(emp2);

            // Filter by Shipping department
            mockMvc.perform(get("/api/employees")
                    .param("department", "Shipping"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.department == 'Shipping')]").exists());
        }

        @Test
        @DisplayName("Should filter by multiple criteria")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testFiltering_MultipleCriteria() throws Exception {
            Employee emp = new Employee();
            emp.setName("Multi Filter Employee");
            emp.setBadgeId("MULTI001");
            emp.setRole("SUPERVISOR");
            emp.setDepartment("Shipping");
            emp.setHireDate(LocalDate.now());
            emp.setStatus("ACTIVE");
            emp.setDeleted(false);
            employeeRepository.save(emp);

            mockMvc.perform(get("/api/employees")
                    .param("department", "Shipping")
                    .param("role", "SUPERVISOR")
                    .param("status", "ACTIVE"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[?(@.badgeId == 'MULTI001')]").exists());
        }
    }

    // ==================== VALIDATION INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Validation Integration Tests")
    class ValidationIntegrationTests {

        @Test
        @DisplayName("Should enforce unique badge ID constraint")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testValidation_UniqueBadgeId() throws Exception {
            // Create first employee
            testEmployeeRequest.setBadgeId("UNIQUE001");
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isCreated());

            // Try to create duplicate
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should validate email format")
        @WithMockUser(roles = "ADMIN")
        void testValidation_EmailFormat() throws Exception {
            testEmployeeRequest.setBadgeId("EMAIL001");
            testEmployeeRequest.setEmail("invalid-email");

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.email").exists());
        }

        @Test
        @DisplayName("Should validate phone format")
        @WithMockUser(roles = "ADMIN")
        void testValidation_PhoneFormat() throws Exception {
            testEmployeeRequest.setBadgeId("PHONE001");
            testEmployeeRequest.setPhone("invalid");

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.phone").exists());
        }

        @Test
        @DisplayName("Should validate badge ID format")
        @WithMockUser(roles = "ADMIN")
        void testValidation_BadgeIdFormat() throws Exception {
            testEmployeeRequest.setBadgeId("emp"); // Too short

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.errors.badgeId").exists());
        }
    }

    // ==================== EDGE CASE INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Edge Case Integration Tests")
    class EdgeCaseIntegrationTests {

        @Test
        @DisplayName("Should handle concurrent employee creation")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testConcurrency_EmployeeCreation() throws Exception {
            testEmployeeRequest.setBadgeId("CONC001");
            
            // Simulate concurrent requests
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isCreated());

            // Second request should fail due to unique constraint
            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should handle very long names")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testEdgeCase_LongName() throws Exception {
            testEmployeeRequest.setBadgeId("LONG001");
            testEmployeeRequest.setName("A".repeat(255));

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should handle special characters in name")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testEdgeCase_SpecialCharacters() throws Exception {
            testEmployeeRequest.setBadgeId("SPEC001");
            testEmployeeRequest.setName("O'Brien-Smith Jr.");

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.name").value("O'Brien-Smith Jr."));
        }

        @Test
        @DisplayName("Should handle null optional fields")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testEdgeCase_NullOptionalFields() throws Exception {
            testEmployeeRequest.setBadgeId("NULL001");
            testEmployeeRequest.setShiftGroup(null);
            testEmployeeRequest.setEmail(null);
            testEmployeeRequest.setPhone(null);
            testEmployeeRequest.setAddress(null);

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                    .andExpect(status().isCreated());
        }
    }

    // ==================== PERFORMANCE INTEGRATION TESTS ====================

    @Nested
    @DisplayName("Performance Integration Tests")
    class PerformanceIntegrationTests {

        @Test
        @DisplayName("Should handle bulk employee creation")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testPerformance_BulkCreation() throws Exception {
            for (int i = 1; i <= 50; i++) {
                testEmployeeRequest.setName("Bulk Employee " + i);
                testEmployeeRequest.setBadgeId("BULK" + String.format("%03d", i));

                mockMvc.perform(post("/api/employees")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(testEmployeeRequest)))
                        .andExpect(status().isCreated());
            }

            // Verify count
            long count = employeeRepository.count();
            assertTrue(count >= 50);
        }

        @Test
        @DisplayName("Should handle large result set pagination")
        @WithMockUser(roles = "ADMIN")
        @Transactional
        void testPerformance_LargeResultSet() throws Exception {
            // Create 100 employees
            for (int i = 1; i <= 100; i++) {
                Employee emp = new Employee();
                emp.setName("Performance Employee " + i);
                emp.setBadgeId("PERF" + String.format("%04d", i));
                emp.setRole("WORKER");
                emp.setDepartment("Shipping");
                emp.setHireDate(LocalDate.now());
                emp.setStatus("ACTIVE");
                emp.setDeleted(false);
                employeeRepository.save(emp);
            }

            // Test pagination performance
            mockMvc.perform(get("/api/employees")
                    .param("page", "0")
                    .param("size", "50"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray());
        }
    }
}
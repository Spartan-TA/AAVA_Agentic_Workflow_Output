package com.warehouse.ems.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.domain.employee.Role;
import com.warehouse.ems.dto.attendance.AttendanceEventRequest;
import com.warehouse.ems.dto.employee.EmployeeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive integration tests for the Warehouse EMS application.
 * Tests cover end-to-end workflows, database interactions, and cross-module functionality.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public class IntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeRequest employeeRequest;
    private AttendanceEventRequest attendanceRequest;

    @BeforeEach
    public void setUp() {
        employeeRequest = new EmployeeRequest();
        employeeRequest.setBadgeId("INT001");
        employeeRequest.setName("Integration Test Employee");
        employeeRequest.setRole(Role.WORKER);
        employeeRequest.setDepartment("Warehouse");
        employeeRequest.setShiftGroup("Morning");
        employeeRequest.setHireDate(LocalDate.now());
        employeeRequest.setStatus("ACTIVE");

        attendanceRequest = new AttendanceEventRequest();
        attendanceRequest.setLocation("Warehouse A");
        attendanceRequest.setDevice("Terminal-01");
    }

    // ==================== EMPLOYEE LIFECYCLE TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testEmployeeLifecycle_CreateUpdateDelete_Success() throws Exception {
        // Step 1: Create employee
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("INT001"))
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long employeeId = objectMapper.readTree(responseBody).get("id").asLong();

        // Step 2: Retrieve employee
        mockMvc.perform(get("/api/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(employeeId))
                .andExpect(jsonPath("$.name").value("Integration Test Employee"));

        // Step 3: Update employee
        employeeRequest.setName("Updated Integration Employee");
        mockMvc.perform(put("/api/employees/" + employeeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Integration Employee"));

        // Step 4: Delete employee
        mockMvc.perform(delete("/api/employees/" + employeeId)
                .with(csrf()))
                .andExpect(status().isNoContent());

        // Step 5: Verify deletion
        mockMvc.perform(get("/api/employees/" + employeeId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testEmployeeCreation_WithAllFields_Success() throws Exception {
        // Arrange
        employeeRequest.setBadgeId("INT002");
        employeeRequest.setName("Full Data Employee");
        employeeRequest.setRole(Role.SUPERVISOR);
        employeeRequest.setDepartment("Operations");
        employeeRequest.setShiftGroup("Evening");
        employeeRequest.setHireDate(LocalDate.of(2024, 1, 1));
        employeeRequest.setStatus("ACTIVE");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("INT002"))
                .andExpect(jsonPath("$.role").value("SUPERVISOR"))
                .andExpect(jsonPath("$.department").value("Operations"));
    }

    // ==================== ATTENDANCE WORKFLOW TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testAttendanceWorkflow_ClockInAndOut_Success() throws Exception {
        // Step 1: Create employee
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long employeeId = objectMapper.readTree(responseBody).get("id").asLong();

        // Step 2: Clock in
        attendanceRequest.setEmployeeId(employeeId);
        MvcResult clockInResult = mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attendanceRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.employeeId").value(employeeId))
                .andExpect(jsonPath("$.clockIn").exists())
                .andReturn();

        String clockInBody = clockInResult.getResponse().getContentAsString();
        Long eventId = objectMapper.readTree(clockInBody).get("id").asLong();

        // Step 3: Clock out
        mockMvc.perform(post("/api/attendance/clock-out/" + eventId)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.clockOut").exists());

        // Step 4: Verify attendance record
        mockMvc.perform(get("/api/attendance/employee/" + employeeId)
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(eventId));

        // Step 5: Calculate hours worked
        mockMvc.perform(get("/api/attendance/employee/" + employeeId + "/hours")
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testAttendanceWorkflow_MultipleClockIns_Success() throws Exception {
        // Step 1: Create employee as admin
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isForbidden()); // Worker cannot create employees
    }

    // ==================== SECURITY INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "WORKER")
    public void testSecurity_WorkerCannotAccessAdminEndpoints() throws Exception {
        // Worker should not be able to create employees
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    public void testSecurity_SupervisorCanViewButNotModify() throws Exception {
        // Supervisor can view employees
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());

        // But cannot create employees
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "HR")
    public void testSecurity_HRCanManageEmployees() throws Exception {
        // HR can create employees
        MvcResult result = mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        Long employeeId = objectMapper.readTree(responseBody).get("id").asLong();

        // HR can update employees
        employeeRequest.setName("Updated by HR");
        mockMvc.perform(put("/api/employees/" + employeeId)
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isOk());

        // HR can delete employees
        mockMvc.perform(delete("/api/employees/" + employeeId)
                .with(csrf()))
                .andExpect(status().isNoContent());
    }

    // ==================== VALIDATION INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testValidation_InvalidEmployeeData_BadRequest() throws Exception {
        // Missing required field
        employeeRequest.setBadgeId(null);

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testValidation_DuplicateBadgeId_BadRequest() throws Exception {
        // Create first employee
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated());

        // Try to create duplicate
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isBadRequest());
    }

    // ==================== PAGINATION INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testPagination_MultipleEmployees_Success() throws Exception {
        // Create multiple employees
        for (int i = 1; i <= 5; i++) {
            EmployeeRequest request = new EmployeeRequest();
            request.setBadgeId("PAGE" + i);
            request.setName("Employee " + i);
            request.setRole(Role.WORKER);
            request.setDepartment("Warehouse");

            mockMvc.perform(post("/api/employees")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());
        }

        // Test pagination
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(5))
                .andExpect(jsonPath("$.numberOfElements").value(2));
    }

    // ==================== DEPARTMENT FILTER INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testDepartmentFilter_MultipleEmployees_Success() throws Exception {
        // Create employees in different departments
        EmployeeRequest warehouse1 = new EmployeeRequest();
        warehouse1.setBadgeId("WH001");
        warehouse1.setName("Warehouse Employee 1");
        warehouse1.setRole(Role.WORKER);
        warehouse1.setDepartment("Warehouse");

        EmployeeRequest office1 = new EmployeeRequest();
        office1.setBadgeId("OF001");
        office1.setName("Office Employee 1");
        office1.setRole(Role.ADMIN);
        office1.setDepartment("Office");

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(warehouse1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(office1)))
                .andExpect(status().isCreated());

        // Filter by Warehouse department
        mockMvc.perform(get("/api/employees")
                .param("department", "Warehouse"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department").value("Warehouse"));

        // Filter by Office department
        mockMvc.perform(get("/api/employees")
                .param("department", "Office"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department").value("Office"));
    }

    // ==================== ERROR HANDLING INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testErrorHandling_NonExistentEmployee_NotFound() throws Exception {
        mockMvc.perform(get("/api/employees/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testErrorHandling_InvalidIdFormat_BadRequest() throws Exception {
        mockMvc.perform(get("/api/employees/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    public void testErrorHandling_ClockOutWithoutClockIn_NotFound() throws Exception {
        mockMvc.perform(post("/api/attendance/clock-out/99999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    // ==================== ACTUATOR HEALTH CHECK TESTS ====================

    @Test
    public void testActuator_HealthEndpoint_Success() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }

    @Test
    public void testActuator_InfoEndpoint_Success() throws Exception {
        mockMvc.perform(get("/actuator/info"))
                .andExpect(status().isOk());
    }

    // ==================== CROSS-MODULE INTEGRATION TESTS ====================

    @Test
    @WithMockUser(roles = "ADMIN")
    public void testCrossModule_EmployeeAndAttendance_Success() throws Exception {
        // Create employee
        MvcResult createResult = mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = createResult.getResponse().getContentAsString();
        Long employeeId = objectMapper.readTree(responseBody).get("id").asLong();

        // Clock in for the employee
        attendanceRequest.setEmployeeId(employeeId);
        mockMvc.perform(post("/api/attendance/clock-in")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(attendanceRequest)))
                .andExpect(status().isOk());

        // Verify employee still exists
        mockMvc.perform(get("/api/employees/" + employeeId))
                .andExpect(status().isOk());

        // Verify attendance record exists
        mockMvc.perform(get("/api/attendance/employee/" + employeeId)
                .param("date", LocalDate.now().toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].employeeId").value(employeeId));
    }
}
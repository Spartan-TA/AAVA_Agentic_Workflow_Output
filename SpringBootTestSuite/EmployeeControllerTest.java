package com.wms.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.employee.domain.Employee;
import com.wms.employee.domain.EmployeeStatus;
import com.wms.employee.dto.EmployeeDto;
import com.wms.employee.repository.EmployeeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private EmployeeRepository employeeRepository;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employee = new Employee();
        employee.setBadgeId("B200");
        employee.setName("Controller User");
        employee.setRole("Worker");
        employee.setDepartment("Packing");
        employee.setShiftGroup("A");
        employee.setHireDate(LocalDate.of(2020, 1, 1));
        employee.setStatus(EmployeeStatus.ACTIVE);
        employee.setDeleted(false);
        employee = employeeRepository.save(employee);
    }

    private EmployeeDto buildValidDto() {
        EmployeeDto dto = new EmployeeDto();
        dto.setBadgeId("B201");
        dto.setName("Jane Controller");
        dto.setRole("Supervisor");
        dto.setDepartment("Shipping");
        dto.setShiftGroup("B");
        dto.setHireDate(LocalDate.of(2021, 2, 2));
        dto.setStatus(EmployeeStatus.ON_LEAVE);
        return dto;
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("testCreateEmployee_ValidInput_Success")
    void testCreateEmployee_ValidInput_Success() throws Exception {
        EmployeeDto dto = buildValidDto();
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("B201"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("testCreateEmployee_DuplicateBadgeId_ThrowsException")
    void testCreateEmployee_DuplicateBadgeId_ThrowsException() throws Exception {
        EmployeeDto dto = buildValidDto();
        dto.setBadgeId("B200"); // Already exists
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError()); // Service throws DuplicateBadgeIdException
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("testCreateEmployee_InvalidInput_ValidationErrors")
    void testCreateEmployee_InvalidInput_ValidationErrors() throws Exception {
        EmployeeDto dto = new EmployeeDto();
        mockMvc.perform(post("/api/v1/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    @DisplayName("testListEmployees_Success")
    void testListEmployees_Success() throws Exception {
        mockMvc.perform(get("/api/v1/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    @DisplayName("testGetEmployeeById_ValidId_Success")
    void testGetEmployeeById_ValidId_Success() throws Exception {
        mockMvc.perform(get("/api/v1/employees/" + employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("B200"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    @DisplayName("testGetEmployeeById_NotFound")
    void testGetEmployeeById_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/employees/99999"))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("testUpdateEmployee_ValidInput_Success")
    void testUpdateEmployee_ValidInput_Success() throws Exception {
        EmployeeDto dto = buildValidDto();
        mockMvc.perform(put("/api/v1/employees/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Controller"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("testUpdateEmployee_NotFound")
    void testUpdateEmployee_NotFound() throws Exception {
        EmployeeDto dto = buildValidDto();
        mockMvc.perform(put("/api/v1/employees/99999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("testPatchEmployee_ValidFields_Success")
    void testPatchEmployee_ValidFields_Success() throws Exception {
        String patchJson = "{"name":"Patched Name","status":"INACTIVE"}";
        mockMvc.perform(patch("/api/v1/employees/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Patched Name"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    @DisplayName("testPatchEmployee_InvalidStatus_ThrowsException")
    void testPatchEmployee_InvalidStatus_ThrowsException() throws Exception {
        String patchJson = "{"status":"INVALID"}";
        mockMvc.perform(patch("/api/v1/employees/" + employee.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(patchJson))
                .andExpect(status().isInternalServerError());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testDeleteEmployee_Success")
    void testDeleteEmployee_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/" + employee.getId()))
                .andExpect(status().isNoContent());
        Employee deleted = employeeRepository.findById(employee.getId()).get();
        assertThat(deleted.getDeleted()).isTrue();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testDeleteEmployee_NotFound")
    void testDeleteEmployee_NotFound() throws Exception {
        mockMvc.perform(delete("/api/v1/employees/99999"))
                .andExpect(status().isInternalServerError());
    }

    @Nested
    @DisplayName("Security Tests")
    class SecurityTests {
        @Test
        @WithMockUser(roles = {"HR"})
        void testDeleteEmployee_ForbiddenForNonAdmin() throws Exception {
            mockMvc.perform(delete("/api/v1/employees/" + employee.getId()))
                    .andExpect(status().isForbidden());
        }

        @Test
        @WithMockUser(roles = {"SUPERVISOR"})
        void testCreateEmployee_ForbiddenForSupervisor() throws Exception {
            EmployeeDto dto = buildValidDto();
            mockMvc.perform(post("/api/v1/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isForbidden());
        }

        @Test
        void testListEmployees_Unauthenticated_Unauthorized() throws Exception {
            mockMvc.perform(get("/api/v1/employees"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("Boundary and Validation Tests")
    class BoundaryTests {
        @Test
        @WithMockUser(roles = {"ADMIN", "HR"})
        void testCreateEmployee_EmptyStrings_ValidationError() throws Exception {
            EmployeeDto dto = new EmployeeDto();
            dto.setBadgeId("");
            dto.setName("");
            dto.setRole("");
            dto.setDepartment("");
            dto.setHireDate(LocalDate.now());
            dto.setStatus(EmployeeStatus.ACTIVE);
            mockMvc.perform(post("/api/v1/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @WithMockUser(roles = {"ADMIN", "HR"})
        void testCreateEmployee_NullFields_ValidationError() throws Exception {
            EmployeeDto dto = new EmployeeDto();
            mockMvc.perform(post("/api/v1/employees")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(dto)))
                    .andExpect(status().isBadRequest());
        }
    }
}

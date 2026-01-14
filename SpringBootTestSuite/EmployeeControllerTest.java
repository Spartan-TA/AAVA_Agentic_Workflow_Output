package com.warehouse.ems.employee;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.employee.dto.CreateEmployeeRequest;
import com.warehouse.ems.employee.dto.EmployeeDTO;
import com.warehouse.ems.employee.dto.UpdateEmployeeRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Comprehensive JUnit test suite for EmployeeController
 * Tests cover REST API endpoints, security, validation, and error handling
 */
@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private Employee testEmployee;
    private CreateEmployeeRequest createRequest;
    private UpdateEmployeeRequest updateRequest;
    private EmployeeDTO employeeDTO;

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

        // Setup create request
        createRequest = new CreateEmployeeRequest();
        createRequest.setBadgeId("EMP001");
        createRequest.setFirstName("John");
        createRequest.setLastName("Doe");
        createRequest.setEmail("john.doe@warehouse.com");
        createRequest.setPhone("+1234567890");
        createRequest.setDepartment("Warehouse");
        createRequest.setRole("WORKER");

        // Setup update request
        updateRequest = new UpdateEmployeeRequest();
        updateRequest.setFirstName("John Updated");
        updateRequest.setLastName("Doe Updated");
        updateRequest.setEmail("john.updated@warehouse.com");

        // Setup DTO
        employeeDTO = new EmployeeDTO();
        employeeDTO.setId(1L);
        employeeDTO.setBadgeId("EMP001");
        employeeDTO.setFirstName("John");
        employeeDTO.setLastName("Doe");
        employeeDTO.setEmail("john.doe@warehouse.com");
    }

    // ========== CREATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testCreateEmployee_ValidRequest_Returns201() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));

        verify(employeeService, times(1)).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateEmployee_InvalidEmail_Returns400() throws Exception {
        // Arrange
        createRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateEmployee_MissingRequiredFields_Returns400() throws Exception {
        // Arrange
        createRequest.setFirstName(null);
        createRequest.setLastName(null);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateEmployee_DuplicateBadgeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.createEmployee(any(Employee.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    public void testCreateEmployee_UnauthorizedRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).createEmployee(any(Employee.class));
    }

    @Test
    public void testCreateEmployee_Unauthenticated_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isUnauthorized());
    }

    // ========== GET EMPLOYEES TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR"})
    public void testGetEmployees_WithPagination_Returns200() throws Exception {
        // Arrange
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeService.getEmployees(any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"))
                .andExpect(jsonPath("$.content[0].firstName").value("John"));

        verify(employeeService, times(1)).getEmployees(any(PageRequest.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testGetEmployees_EmptyResult_Returns200() throws Exception {
        // Arrange
        Page<Employee> emptyPage = new PageImpl<>(Arrays.asList());
        when(employeeService.getEmployees(any(PageRequest.class))).thenReturn(emptyPage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testGetEmployees_InvalidPageNumber_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "-1")
                .param("size", "10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    public void testGetEmployees_UnauthorizedRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    // ========== GET EMPLOYEE BY ID TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN", "HR", "SUPERVISOR", "WORKER"})
    public void testGetEmployee_ValidId_Returns200() throws Exception {
        // Arrange
        when(employeeService.getEmployee(1L)).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.badgeId").value("EMP001"))
                .andExpect(jsonPath("$.firstName").value("John"));

        verify(employeeService, times(1)).getEmployee(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testGetEmployee_InvalidId_Returns404() throws Exception {
        // Arrange
        when(employeeService.getEmployee(999L)).thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/999"))
                .andExpect(status().isNotFound());

        verify(employeeService, times(1)).getEmployee(999L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testGetEmployee_NegativeId_Returns400() throws Exception {
        // Arrange
        when(employeeService.getEmployee(-1L)).thenThrow(new IllegalArgumentException("Invalid ID"));

        // Act & Assert
        mockMvc.perform(get("/api/employees/-1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetEmployee_Unauthenticated_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isUnauthorized());
    }

    // ========== UPDATE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN", "HR"})
    public void testUpdateEmployee_ValidRequest_Returns200() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));

        verify(employeeService, times(1)).updateEmployee(anyLong(), any(Employee.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUpdateEmployee_InvalidId_Returns404() throws Exception {
        // Arrange
        when(employeeService.updateEmployee(anyLong(), any(Employee.class)))
                .thenThrow(new NoSuchElementException("Employee not found"));

        // Act & Assert
        mockMvc.perform(put("/api/employees/999")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUpdateEmployee_InvalidEmail_Returns400() throws Exception {
        // Arrange
        updateRequest.setEmail("invalid-email");

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isBadRequest());

        verify(employeeService, never()).updateEmployee(anyLong(), any(Employee.class));
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    public void testUpdateEmployee_UnauthorizedRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testUpdateEmployee_PartialUpdate_Returns200() throws Exception {
        // Arrange
        UpdateEmployeeRequest partialUpdate = new UpdateEmployeeRequest();
        partialUpdate.setEmail("new.email@warehouse.com");
        when(employeeService.updateEmployee(anyLong(), any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(put("/api/employees/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(partialUpdate)))
                .andExpect(status().isOk());
    }

    // ========== DELETE EMPLOYEE TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteEmployee_ValidId_Returns204() throws Exception {
        // Arrange
        doNothing().when(employeeService).deleteEmployee(1L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testDeleteEmployee_InvalidId_Returns404() throws Exception {
        // Arrange
        doThrow(new NoSuchElementException("Employee not found")).when(employeeService).deleteEmployee(999L);

        // Act & Assert
        mockMvc.perform(delete("/api/employees/999")
                .with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    public void testDeleteEmployee_UnauthorizedRole_Returns403() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isForbidden());

        verify(employeeService, never()).deleteEmployee(anyLong());
    }

    @Test
    public void testDeleteEmployee_Unauthenticated_Returns401() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/employees/1")
                .with(csrf()))
                .andExpect(status().isUnauthorized());
    }

    // ========== CONTENT TYPE TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateEmployee_UnsupportedMediaType_Returns415() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.TEXT_PLAIN)
                .content("plain text"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateEmployee_MalformedJson_Returns400() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isBadRequest());
    }

    // ========== BOUNDARY CONDITION TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testGetEmployees_MaxPageSize_Returns200() throws Exception {
        // Arrange
        Page<Employee> employeePage = new PageImpl<>(Arrays.asList(testEmployee));
        when(employeeService.getEmployees(any(PageRequest.class))).thenReturn(employeePage);

        // Act & Assert
        mockMvc.perform(get("/api/employees")
                .param("page", "0")
                .param("size", "1000"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateEmployee_MaxLengthFields_Returns201() throws Exception {
        // Arrange
        String maxLengthString = "A".repeat(255);
        createRequest.setFirstName(maxLengthString);
        createRequest.setLastName(maxLengthString);
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateEmployee_EmptyStringFields_Returns400() throws Exception {
        // Arrange
        createRequest.setFirstName("");
        createRequest.setLastName("");

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isBadRequest());
    }

    // ========== SPECIAL CHARACTER TESTS ==========

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateEmployee_SpecialCharactersInName_Returns201() throws Exception {
        // Arrange
        createRequest.setFirstName("Jean-Pierre");
        createRequest.setLastName("O'Connor");
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    public void testCreateEmployee_InternationalCharacters_Returns201() throws Exception {
        // Arrange
        createRequest.setFirstName("JosÃ©");
        createRequest.setLastName("MÃ¼ller");
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(testEmployee);

        // Act & Assert
        mockMvc.perform(post("/api/employees")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());
    }
}
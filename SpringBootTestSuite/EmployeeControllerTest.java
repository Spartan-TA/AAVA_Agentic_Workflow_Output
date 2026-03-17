package SpringBootTestSuite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wms.ems.employee.controller.EmployeeController;
import com.wms.ems.employee.dto.EmployeeDTO;
import com.wms.ems.employee.model.Role;
import com.wms.ems.employee.model.Status;
import com.wms.ems.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean EmployeeService employeeService;
    @Autowired ObjectMapper objectMapper;

    EmployeeDTO validDto;

    @BeforeEach
    void setup() {
        validDto = EmployeeDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role(Role.WORKER)
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status(Status.ACTIVE)
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .build();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - create employee with valid input")
    void testCreateEmployeeValid() throws Exception {
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(validDto);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - create employee with duplicate badge ID")
    void testCreateEmployeeDuplicateBadgeId() throws Exception {
        when(employeeService.create(any(EmployeeDTO.class))).thenThrow(new com.wms.ems.exception.BadRequestException("Badge ID already exists"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Badge ID already exists"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - create employee with null badge ID")
    void testCreateEmployeeNullBadgeId() throws Exception {
        EmployeeDTO dto = EmployeeDTO.builder().name("Jane").badgeId(null).role(Role.WORKER).hireDate(LocalDate.now()).status(Status.ACTIVE).build();
        when(employeeService.create(any(EmployeeDTO.class))).thenThrow(new IllegalArgumentException("Badge ID is required"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /employees/{id} - update employee with valid input")
    void testUpdateEmployeeValid() throws Exception {
        when(employeeService.update(eq(1L), any(EmployeeDTO.class))).thenReturn(validDto);
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("PUT /employees/{id} - update employee not found")
    void testUpdateEmployeeNotFound() throws Exception {
        when(employeeService.update(eq(1L), any(EmployeeDTO.class))).thenThrow(new com.wms.ems.exception.ResourceNotFoundException("Employee not found"));
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /employees/{id} - delete employee with valid input")
    void testDeleteEmployeeValid() throws Exception {
        doNothing().when(employeeService).delete(1L);
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("DELETE /employees/{id} - delete employee not found")
    void testDeleteEmployeeNotFound() throws Exception {
        Mockito.doThrow(new com.wms.ems.exception.ResourceNotFoundException("Employee not found")).when(employeeService).delete(1L);
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /employees/{id} - find employee by ID with valid input")
    void testFindEmployeeByIdValid() throws Exception {
        when(employeeService.findById(1L)).thenReturn(validDto);
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    @Test
    @WithMockUser(roles = "USER")
    @DisplayName("GET /employees/{id} - find employee by ID not found")
    void testFindEmployeeByIdNotFound() throws Exception {
        when(employeeService.findById(1L)).thenThrow(new com.wms.ems.exception.ResourceNotFoundException("Employee not found"));
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found"));
    }

    @Test
    @DisplayName("POST /employees - forbidden for unauthenticated user")
    void testCreateEmployeeForbidden() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - create employee with empty name")
    void testCreateEmployeeEmptyName() throws Exception {
        EmployeeDTO dto = EmployeeDTO.builder().name("").badgeId("BADGE124").role(Role.WORKER).hireDate(LocalDate.now()).status(Status.ACTIVE).build();
        when(employeeService.create(any(EmployeeDTO.class))).thenThrow(new IllegalArgumentException("Employee name is required"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - create employee with boundary badge ID length")
    void testCreateEmployeeBoundaryBadgeIdLength() throws Exception {
        String badgeId = "B".repeat(50);
        EmployeeDTO dto = EmployeeDTO.builder().name("Jane Doe").badgeId(badgeId).role(Role.WORKER).hireDate(LocalDate.now()).status(Status.ACTIVE).build();
        when(employeeService.create(any(EmployeeDTO.class))).thenReturn(dto);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value(badgeId));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - create employee with invalid role")
    void testCreateEmployeeInvalidRole() throws Exception {
        EmployeeDTO dto = EmployeeDTO.builder().name("Jane Doe").badgeId("BADGE125").role(null).hireDate(LocalDate.now()).status(Status.ACTIVE).build();
        when(employeeService.create(any(EmployeeDTO.class))).thenThrow(new IllegalArgumentException("Role is required"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - create employee with null hire date")
    void testCreateEmployeeNullHireDate() throws Exception {
        EmployeeDTO dto = EmployeeDTO.builder().name("Jane Doe").badgeId("BADGE126").role(Role.WORKER).hireDate(null).status(Status.ACTIVE).build();
        when(employeeService.create(any(EmployeeDTO.class))).thenThrow(new IllegalArgumentException("Hire date is required"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("POST /employees - create employee with null status")
    void testCreateEmployeeNullStatus() throws Exception {
        EmployeeDTO dto = EmployeeDTO.builder().name("Jane Doe").badgeId("BADGE127").role(Role.WORKER).hireDate(LocalDate.now()).status(null).build();
        when(employeeService.create(any(EmployeeDTO.class))).thenThrow(new IllegalArgumentException("Status is required"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    // Add more tests for pagination, filtering, and edge cases as needed
}

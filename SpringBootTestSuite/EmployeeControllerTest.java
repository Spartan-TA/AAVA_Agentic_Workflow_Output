package com.warehouse.employee.management;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee.management.dto.EmployeeRequestDto;
import com.warehouse.employee.management.dto.EmployeeResponseDto;
import com.warehouse.employee.management.exception.ResourceNotFoundException;
import com.warehouse.employee.management.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeRequestDto employeeRequestDto;
    private EmployeeResponseDto employeeResponseDto;

    @BeforeEach
    public void setUp() {
        employeeRequestDto = new EmployeeRequestDto();
        employeeRequestDto.setName("John Doe");
        employeeRequestDto.setBadgeId("BADGE123");
        employeeRequestDto.setRole("WORKER");
        employeeRequestDto.setDepartment("Logistics");
        employeeRequestDto.setShiftGroup("A");
        employeeRequestDto.setHireDate(LocalDate.of(2022, 1, 1));

        employeeResponseDto = new EmployeeResponseDto();
        employeeResponseDto.setId(1L);
        employeeResponseDto.setName("John Doe");
        employeeResponseDto.setBadgeId("BADGE123");
        employeeResponseDto.setRole("WORKER");
        employeeResponseDto.setDepartment("Logistics");
        employeeResponseDto.setShiftGroup("A");
        employeeResponseDto.setHireDate(LocalDate.of(2022, 1, 1));
        employeeResponseDto.setStatus("ACTIVE");
    }

    @Test
    public void testPostEmployees_WithValidRequest_ShouldReturnCreated() throws Exception {
        Mockito.when(employeeService.createEmployee(any(EmployeeRequestDto.class))).thenReturn(employeeResponseDto);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testPostEmployees_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        EmployeeRequestDto invalidDto = new EmployeeRequestDto();
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testPostEmployees_WithDuplicateBadgeId_ShouldReturnConflict() throws Exception {
        Mockito.when(employeeService.createEmployee(any(EmployeeRequestDto.class)))
                .thenThrow(new IllegalArgumentException("Badge ID already exists"));
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isConflict());
    }

    @Test
    public void testGetEmployees_WithPagination_ShouldReturnOk() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Mockito.when(employeeService.getAllEmployees(any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(employeeResponseDto)));
        mockMvc.perform(get("/api/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    public void testGetEmployeeById_WithValidId_ShouldReturnOk() throws Exception {
        Mockito.when(employeeService.getEmployeeById(eq(1L))).thenReturn(employeeResponseDto);
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testGetEmployeeById_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        Mockito.when(employeeService.getEmployeeById(eq(99L))).thenThrow(new ResourceNotFoundException("Employee not found"));
        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testPutEmployee_WithValidData_ShouldReturnOk() throws Exception {
        Mockito.when(employeeService.updateEmployee(eq(1L), any(EmployeeRequestDto.class))).thenReturn(employeeResponseDto);
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    public void testPutEmployee_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        Mockito.when(employeeService.updateEmployee(eq(99L), any(EmployeeRequestDto.class)))
                .thenThrow(new ResourceNotFoundException("Employee not found"));
        mockMvc.perform(put("/api/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeRequestDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteEmployee_WithValidId_ShouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(employeeService).softDeleteEmployee(eq(1L));
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteEmployee_WithNonExistentId_ShouldReturnNotFound() throws Exception {
        Mockito.doThrow(new ResourceNotFoundException("Employee not found")).when(employeeService).softDeleteEmployee(eq(99L));
        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetEmployeesFilter_WithStatus_ShouldReturnOk() throws Exception {
        Mockito.when(employeeService.filterByStatus(eq("ACTIVE"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(employeeResponseDto)));
        mockMvc.perform(get("/api/employees/filter?status=ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("ACTIVE"));
    }

    @Test
    public void testGetEmployeesFilter_WithDepartment_ShouldReturnOk() throws Exception {
        Mockito.when(employeeService.filterByDepartment(eq("Logistics"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(Collections.singletonList(employeeResponseDto)));
        mockMvc.perform(get("/api/employees/filter?department=Logistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department").value("Logistics"));
    }
}

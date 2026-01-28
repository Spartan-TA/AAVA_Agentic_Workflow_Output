package com.warehouse.employee.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee.dto.*;
import com.warehouse.employee.exception.DuplicateException;
import com.warehouse.employee.exception.NotFoundException;
import com.warehouse.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.time.LocalDate;
import java.util.*;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeCreateRequest validCreateRequest;
    private EmployeeUpdateRequest validUpdateRequest;
    private EmployeeDTO employeeDTO;
    private UUID employeeId;
    private String actor;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        actor = "test-actor";
        validCreateRequest = EmployeeCreateRequest.builder()
                .name("John Doe")
                .badgeId("BADGE123")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("Active")
                .build();
        validUpdateRequest = EmployeeUpdateRequest.builder()
                .name("Jane Doe")
                .role("Supervisor")
                .department("Shipping")
                .shiftGroup("B")
                .status("Inactive")
                .build();
        employeeDTO = EmployeeDTO.builder()
                .id(employeeId)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("Worker")
                .department("Packing")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status("Active")
                .build();
    }

    @Test
    void testCreateEmployee_WithValidRequest_ShouldReturn201() throws Exception {
        when(employeeService.createEmployee(any(EmployeeCreateRequest.class), eq(actor))).thenReturn(employeeDTO);
        mockMvc.perform(post("/employees")
                .header("X-Actor", actor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(validCreateRequest.getName())))
                .andExpect(jsonPath("$.badgeId", is(validCreateRequest.getBadgeId())));
    }

    @Test
    void testCreateEmployee_WithInvalidRequest_ShouldReturn400() throws Exception {
        EmployeeCreateRequest invalidRequest = EmployeeCreateRequest.builder().build();
        mockMvc.perform(post("/employees")
                .header("X-Actor", actor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.badgeId").exists())
                .andExpect(jsonPath("$.role").exists())
                .andExpect(jsonPath("$.status").exists());
    }

    @Test
    void testCreateEmployee_WithDuplicateBadgeId_ShouldReturn409() throws Exception {
        when(employeeService.createEmployee(any(EmployeeCreateRequest.class), eq(actor)))
                .thenThrow(new DuplicateException("Employee with badgeId BADGE123 already exists"));
        mockMvc.perform(post("/employees")
                .header("X-Actor", actor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error", containsString("badgeId")));
    }

    @Test
    void testCreateEmployee_WithoutActorHeader_ShouldReturn400() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validCreateRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testUpdateEmployee_WithValidRequest_ShouldReturn200() throws Exception {
        when(employeeService.updateEmployee(eq(employeeId), any(EmployeeUpdateRequest.class), eq(actor)))
                .thenReturn(employeeDTO.toBuilder().name(validUpdateRequest.getName()).build());
        mockMvc.perform(put("/employees/" + employeeId)
                .header("X-Actor", actor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is(validUpdateRequest.getName())));
    }

    @Test
    void testUpdateEmployee_WithNonExistentId_ShouldReturn404() throws Exception {
        when(employeeService.updateEmployee(eq(employeeId), any(EmployeeUpdateRequest.class), eq(actor)))
                .thenThrow(new NotFoundException("Employee not found with id: " + employeeId));
        mockMvc.perform(put("/employees/" + employeeId)
                .header("X-Actor", actor)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUpdateRequest)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(employeeId.toString())));
    }

    @Test
    void testGetEmployee_WithValidId_ShouldReturn200() throws Exception {
        when(employeeService.getEmployee(employeeId)).thenReturn(employeeDTO);
        mockMvc.perform(get("/employees/" + employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employeeId.toString())))
                .andExpect(jsonPath("$.name", is(employeeDTO.getName())));
    }

    @Test
    void testGetEmployee_WithNonExistentId_ShouldReturn404() throws Exception {
        when(employeeService.getEmployee(employeeId)).thenThrow(new NotFoundException("Employee not found with id: " + employeeId));
        mockMvc.perform(get("/employees/" + employeeId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(employeeId.toString())));
    }

    @Test
    void testDeleteEmployee_WithValidId_ShouldReturn204() throws Exception {
        doNothing().when(employeeService).deleteEmployee(employeeId, actor);
        mockMvc.perform(delete("/employees/" + employeeId)
                .header("X-Actor", actor))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteEmployee_WithNonExistentId_ShouldReturn404() throws Exception {
        doThrow(new NotFoundException("Employee not found with id: " + employeeId))
                .when(employeeService).deleteEmployee(employeeId, actor);
        mockMvc.perform(delete("/employees/" + employeeId)
                .header("X-Actor", actor))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", containsString(employeeId.toString())));
    }

    @Test
    void testListEmployees_WithPagination_ShouldReturn200() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(List.of(employeeDTO), PageRequest.of(0, 10), 1);
        when(employeeService.listEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].id", is(employeeId.toString())));
    }

    @Test
    void testListEmployees_WithInvalidPageSize_ShouldHandleGracefully() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 0), 0);
        when(employeeService.listEmployees(any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/employees?page=0&size=0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }
}

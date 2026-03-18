package com.example.warehouse.controller;

import com.example.warehouse.dto.EmployeeRequestDto;
import com.example.warehouse.dto.EmployeeResponseDto;
import com.example.warehouse.service.EmployeeService;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class EmployeeControllerTest {

    @Mock private EmployeeService employeeService;
    @InjectMocks private EmployeeController employeeController;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    void createEmployee_ShouldReturnCreatedEmployee() throws Exception {
        EmployeeRequestDto requestDto = new EmployeeRequestDto();
        requestDto.setBadgeId("EMP001");
        EmployeeResponseDto responseDto = new EmployeeResponseDto();
        responseDto.setId(1L);
        responseDto.setBadgeId("EMP001");

        when(employeeService.createEmployee(any())).thenReturn(responseDto);

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john.doe@example.com"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    void getEmployeeById_ShouldReturnEmployee() throws Exception {
        EmployeeResponseDto responseDto = new EmployeeResponseDto();
        responseDto.setId(1L);
        responseDto.setBadgeId("EMP001");

        when(employeeService.getEmployeeById(1L)).thenReturn(responseDto);

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    void getAllEmployees_ShouldReturnPagedEmployees() throws Exception {
        EmployeeResponseDto responseDto = new EmployeeResponseDto();
        responseDto.setId(1L);
        responseDto.setBadgeId("EMP001");
        Page<EmployeeResponseDto> page = new PageImpl<>(Collections.singletonList(responseDto));

        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("EMP001"));
    }

    @Test
    void updateEmployee_ShouldReturnUpdatedEmployee() throws Exception {
        EmployeeRequestDto requestDto = new EmployeeRequestDto();
        EmployeeResponseDto responseDto = new EmployeeResponseDto();
        responseDto.setId(1L);
        responseDto.setBadgeId("EMP001");

        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(responseDto);

        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john.doe@example.com"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    void softDeleteEmployee_ShouldReturnNoContent() throws Exception {
        doNothing().when(employeeService).softDeleteEmployee(1L);

        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void createEmployee_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"","firstName":"","lastName":"","email":"invalid-email"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getEmployeeById_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenThrow(new ResourceNotFoundException("Employee not found"));

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void updateEmployee_WithNonExistingId_ShouldReturnNotFound() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any())).thenThrow(new ResourceNotFoundException("Employee not found"));

        mockMvc.perform(put("/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john.doe@example.com"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createEmployee_WithDuplicateBadgeId_ShouldReturnConflict() throws Exception {
        when(employeeService.createEmployee(any())).thenThrow(new BusinessValidationException("Duplicate badge ID"));

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{"badgeId":"EMP001","firstName":"John","lastName":"Doe","email":"john.doe@example.com"}"))
                .andExpect(status().isConflict());
    }
}
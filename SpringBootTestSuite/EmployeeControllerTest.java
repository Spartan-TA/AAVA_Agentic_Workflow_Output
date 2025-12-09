package com.example.ems.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class EmployeeControllerTest {

    @Mock
    private EmployeeService employeeService;

    private MockMvc mockMvc;

    @InjectMocks
    private EmployeeController employeeController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        mockMvc = MockMvcBuilders.standaloneSetup(employeeController).build();
    }

    @Test
    public void testGetAllEmployeesReturnsOk() throws Exception {
        EmployeeDto dto = new EmployeeDto(1L, "John", "Doe", "john.doe@example.com", "Developer", LocalDate.of(2020, 1, 1));
        when(employeeService.getAllEmployees()).thenReturn(Arrays.asList(dto));

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].firstName").value("John"))
                .andExpect(jsonPath("$[0].email").value("john.doe@example.com"));
    }

    @Test
    public void testGetAllEmployeesReturnsEmptyList() throws Exception {
        when(employeeService.getAllEmployees()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    @Test
    public void testGetEmployeeByIdReturnsOk() throws Exception {
        EmployeeDto dto = new EmployeeDto(1L, "John", "Doe", "john.doe@example.com", "Developer", LocalDate.of(2020, 1, 1));
        when(employeeService.getEmployeeById(1L)).thenReturn(dto);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    public void testGetEmployeeByIdNotFound() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenThrow(new EmployeeNotFoundException("Not found"));

        mockMvc.perform(get("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetEmployeeByIdWithInvalidId() throws Exception {
        mockMvc.perform(get("/api/employees/invalid"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testCreateEmployeeReturnsCreated() throws Exception {
        EmployeeDto saved = new EmployeeDto(2L, "Jane", "Smith", "jane.smith@example.com", "Manager", LocalDate.of(2021, 5, 15));
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenReturn(saved);

        String json = "{"firstName":"Jane","lastName":"Smith","email":"jane.smith@example.com","role":"Manager","hireDate":"2021-05-15"}";

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.firstName").value("Jane"));
    }

    @Test
    public void testCreateEmployeeDuplicate() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDto.class))).thenThrow(new DuplicateEmployeeException("Duplicate"));

        String json = "{"firstName":"Jane","lastName":"Smith","email":"jane.smith@example.com","role":"Manager","hireDate":"2021-05-15"}";

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isConflict());
    }

    @Test
    public void testCreateEmployeeWithInvalidJson() throws Exception {
        String invalidJson = "{"firstName":"Jane","lastName":}";

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testUpdateEmployeeReturnsOk() throws Exception {
        EmployeeDto dto = new EmployeeDto(1L, "Johnny", "Doe", "john.doe@example.com", "Lead", LocalDate.of(2020, 1, 1));
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDto.class))).thenReturn(dto);

        String json = "{"firstName":"Johnny","lastName":"Doe","email":"john.doe@example.com","role":"Lead","hireDate":"2020-01-01"}";

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Johnny"))
                .andExpect(jsonPath("$.role").value("Lead"));
    }

    @Test
    public void testUpdateEmployeeNotFound() throws Exception {
        when(employeeService.updateEmployee(eq(99L), any(EmployeeDto.class))).thenThrow(new EmployeeNotFoundException("Not found"));

        String json = "{"firstName":"Ghost","lastName":"User","email":"ghost@example.com","role":"None","hireDate":"2021-01-01"}";

        mockMvc.perform(put("/api/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteEmployeeReturnsNoContent() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    public void testDeleteEmployeeNotFound() throws Exception {
        doThrow(new EmployeeNotFoundException("Not found")).when(employeeService).deleteEmployee(99L);

        mockMvc.perform(delete("/api/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testDeleteEmployeeWithInvalidId() throws Exception {
        mockMvc.perform(delete("/api/employees/invalid"))
                .andExpect(status().isBadRequest());
    }
}
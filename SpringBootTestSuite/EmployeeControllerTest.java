package com.warehouse.ems.test;

import com.warehouse.ems.employee.EmployeeController;
import com.warehouse.ems.employee.EmployeeService;
import com.warehouse.ems.employee.Employee;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.time.LocalDate;
import java.util.*;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(EmployeeController.class)
@DisplayName("EmployeeController REST API Tests")
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private Employee validEmployee;
    private ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        validEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Shipping")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusYears(1))
                .status("ACTIVE")
                .build();
    }

    @Test
    @DisplayName("POST /employees with valid data should return 201")
    void testCreateEmployeeWithValidData() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(validEmployee);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("POST /employees with null name should return 400")
    void testCreateEmployeeWithNullName_ShouldReturnBadRequest() throws Exception {
        Employee emp = validEmployee.toBuilder().name(null).build();
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /employees/{id} with valid id should return employee")
    void testGetEmployeeById() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(validEmployee);
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    @Test
    @DisplayName("GET /employees/{id} with invalid id should return 404")
    void testGetEmployeeByIdNotFound_ShouldReturnNotFound() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenThrow(new NoSuchElementException());
        mockMvc.perform(get("/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /employees/{id} should update employee")
    void testUpdateEmployee() throws Exception {
        Employee updated = validEmployee.toBuilder().department("Receiving").build();
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(updated);
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department").value("Receiving"));
    }

    @Test
    @DisplayName("PATCH /employees/{id}/soft-delete should set status to INACTIVE")
    void testSoftDeleteEmployee() throws Exception {
        Employee inactive = validEmployee.toBuilder().status("INACTIVE").build();
        when(employeeService.softDeleteEmployee(1L)).thenReturn(inactive);
        mockMvc.perform(patch("/employees/1/soft-delete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("INACTIVE"));
    }

    @Test
    @DisplayName("GET /employees?department=Shipping should filter by department")
    void testGetEmployeesByDepartment() throws Exception {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeService.getEmployeesByDepartment("Shipping")).thenReturn(employees);
        mockMvc.perform(get("/employees?department=Shipping"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].department").value("Shipping"));
    }

    @Test
    @DisplayName("GET /employees?page=0&size=10 should paginate results")
    void testGetEmployeesWithPagination() throws Exception {
        List<Employee> employees = Arrays.asList(validEmployee);
        when(employeeService.getEmployeesPaged(0, 10)).thenReturn(employees);
        mockMvc.perform(get("/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("John Doe"));
    }

    @Test
    @DisplayName("POST /employees with duplicate badgeId should return 409")
    void testCreateEmployeeWithDuplicateBadgeId_ShouldReturnConflict() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenThrow(new org.springframework.dao.DataIntegrityViolationException("Duplicate badgeId"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployee)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("POST /employees with invalid role should return 400")
    void testCreateEmployeeWithInvalidRole_ShouldReturnBadRequest() throws Exception {
        Employee emp = validEmployee.toBuilder().role("INVALID_ROLE").build();
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /employees with empty name should return 400")
    void testCreateEmployeeWithEmptyName_ShouldReturnBadRequest() throws Exception {
        Employee emp = validEmployee.toBuilder().name("").build();
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /employees with null badgeId should return 400")
    void testCreateEmployeeWithNullBadgeId_ShouldReturnBadRequest() throws Exception {
        Employee emp = validEmployee.toBuilder().badgeId(null).build();
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /employees with future hireDate should return 400")
    void testCreateEmployeeWithFutureHireDate_ShouldReturnBadRequest() throws Exception {
        Employee emp = validEmployee.toBuilder().hireDate(LocalDate.now().plusDays(1)).build();
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emp)))
                .andExpect(status().isBadRequest());
    }
}

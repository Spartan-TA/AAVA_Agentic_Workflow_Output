package SpringBootTestSuite;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

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

    private ObjectMapper objectMapper;

    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validEmployee = new Employee(1L, "John Doe", "BADGE123", "WORKER", "Logistics", "A", LocalDate.now(), "ACTIVE");
    }

    @Test
    @DisplayName("Create employee with valid data returns 201")
    void testCreateEmployee_WithValidData_ReturnsCreated() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenReturn(validEmployee);

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployee)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("BADGE123"));
    }

    @Test
    @DisplayName("Create employee with duplicate badgeId returns 409")
    void testCreateEmployee_WithDuplicateBadgeId_ReturnsConflict() throws Exception {
        when(employeeService.createEmployee(any(Employee.class))).thenThrow(new DuplicateBadgeIdException());

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployee)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("Create employee with null fields returns 400")
    void testCreateEmployee_WithNullFields_ReturnsBadRequest() throws Exception {
        Employee invalidEmployee = new Employee(null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEmployee)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Read existing employee returns 200")
    void testReadEmployee_ExistingId_ReturnsOk() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(validEmployee));

        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    @DisplayName("Read non-existing employee returns 404")
    void testReadEmployee_NonExistingId_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Read soft-deleted employee returns 404")
    void testReadEmployee_SoftDeleted_ReturnsNotFound() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("Update employee with valid data returns 200")
    void testUpdateEmployee_WithValidData_ReturnsOk() throws Exception {
        Employee updated = new Employee(1L, "Jane Doe", "BADGE123", "HR", "HR", "B", LocalDate.now(), "ACTIVE");
        when(employeeService.updateEmployee(eq(1L), any(Employee.class))).thenReturn(Optional.of(updated));

        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    @DisplayName("Update employee with invalid data returns 400")
    void testUpdateEmployee_WithInvalidData_ReturnsBadRequest() throws Exception {
        Employee invalid = new Employee(1L, "", "", "", "", "", null, "");

        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Delete employee (soft delete) returns 204")
    void testDeleteEmployee_SoftDelete_ReturnsNoContent() throws Exception {
        doNothing().when(employeeService).softDeleteEmployee(1L);

        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("Delete already deleted employee returns 404")
    void testDeleteEmployee_AlreadyDeleted_ReturnsNotFound() throws Exception {
        doThrow(new EmployeeNotFoundException()).when(employeeService).softDeleteEmployee(99L);

        mockMvc.perform(delete("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("List employees with pagination returns paged result")
    void testListEmployees_Pagination_ReturnsPagedResult() throws Exception {
        when(employeeService.listEmployees(any(Pageable.class), anyMap()))
                .thenReturn(new PageImpl<>(Arrays.asList(validEmployee), PageRequest.of(0, 10), 1));

        mockMvc.perform(get("/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("BADGE123"));
    }

    @Test
    @DisplayName("List employees with filter returns filtered result")
    void testListEmployees_Filtering_ReturnsFilteredResult() throws Exception {
        when(employeeService.listEmployees(any(Pageable.class), anyMap()))
                .thenReturn(new PageImpl<>(Collections.singletonList(validEmployee)));

        mockMvc.perform(get("/employees?role=WORKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].role").value("WORKER"));
    }

    @Test
    @DisplayName("List employees with no results returns empty list")
    void testListEmployees_EmptyResults_ReturnsEmptyList() throws Exception {
        when(employeeService.listEmployees(any(Pageable.class), anyMap()))
                .thenReturn(new PageImpl<>(Collections.emptyList()));

        mockMvc.perform(get("/employees?department=Unknown"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }
}
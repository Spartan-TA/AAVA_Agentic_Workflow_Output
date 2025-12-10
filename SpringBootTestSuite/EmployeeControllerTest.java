import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Arrays;
import java.util.Collections;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EmployeeService employeeService;
    @Autowired
    private ObjectMapper objectMapper;
    private EmployeeDto validEmployeeDto;
    private Employee validEmployee;

    @BeforeEach
    void setUp() {
        validEmployeeDto = new EmployeeDto();
        validEmployeeDto.setName("John Doe");
        validEmployeeDto.setBadgeId("EMP001");
        validEmployeeDto.setRole("Worker");
        validEmployeeDto.setDepartment("Warehouse");
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("John Doe");
        validEmployee.setBadgeId("EMP001");
        validEmployee.setRole("Worker");
        validEmployee.setDepartment("Warehouse");
    }

    @Test
    void testCreateEmployee_ValidRequest() throws Exception {
        when(employeeService.create(any(EmployeeDto.class))).thenReturn(validEmployee);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testCreateEmployee_InvalidRequest() throws Exception {
        EmployeeDto invalidDto = new EmployeeDto();
        invalidDto.setName("");
        invalidDto.setBadgeId("");
        when(employeeService.create(any(EmployeeDto.class))).thenThrow(new ValidationException("Invalid input"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetEmployee_ValidId() throws Exception {
        when(employeeService.get(1L)).thenReturn(validEmployee);
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testGetEmployee_NonExistentId() throws Exception {
        when(employeeService.get(2L)).thenThrow(new ResourceNotFoundException("Not found"));
        mockMvc.perform(get("/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testListEmployees_WithPagination() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        when(employeeService.list(any(Pageable.class))).thenReturn(new PageImpl<>(Arrays.asList(validEmployee)));
        mockMvc.perform(get("/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void testUpdateEmployee_ValidRequest() throws Exception {
        when(employeeService.update(eq(1L), any(EmployeeDto.class))).thenReturn(validEmployee);
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"));
    }

    @Test
    void testUpdateEmployee_NonExistentId() throws Exception {
        when(employeeService.update(eq(2L), any(EmployeeDto.class))).thenThrow(new ResourceNotFoundException("Not found"));
        mockMvc.perform(put("/employees/2")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteEmployee_ValidId() throws Exception {
        doNothing().when(employeeService).delete(1L);
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteEmployee_NonExistentId() throws Exception {
        doThrow(new ResourceNotFoundException("Not found")).when(employeeService).delete(2L);
        mockMvc.perform(delete("/employees/2"))
                .andExpect(status().isNotFound());
    }
}
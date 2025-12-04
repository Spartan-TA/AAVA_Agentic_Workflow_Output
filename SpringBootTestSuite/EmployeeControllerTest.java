import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.ems.employee.dto.EmployeeCreateDto;
import com.warehouse.ems.employee.dto.EmployeeDto;
import com.warehouse.ems.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private EmployeeService employeeService;

    @Autowired
    private ObjectMapper objectMapper;

    private EmployeeCreateDto employeeCreateDto;
    private EmployeeDto employeeDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        employeeCreateDto = new EmployeeCreateDto();
        employeeCreateDto.setBadgeId("12345");
        employeeCreateDto.setName("John Doe");
        employeeCreateDto.setEmail("john.doe@example.com");
        employeeCreateDto.setPhone("123-456-7890");
        employeeCreateDto.setRole("Manager");
        employeeCreateDto.setDepartment("Operations");
        employeeCreateDto.setHireDate(LocalDate.of(2020, 1, 1));
        employeeCreateDto.setStatus("Active");

        employeeDto = new EmployeeDto();
        employeeDto.setId(1L);
        employeeDto.setBadgeId("12345");
        employeeDto.setName("John Doe");
        employeeDto.setEmail("john.doe@example.com");
        employeeDto.setPhone("123-456-7890");
        employeeDto.setRole("Manager");
        employeeDto.setDepartment("Operations");
        employeeDto.setHireDate(LocalDate.of(2020, 1, 1));
        employeeDto.setStatus("Active");
    }

    @Test
    void testCreateEmployee() throws Exception {
        when(employeeService.createEmployee(any(EmployeeCreateDto.class))).thenReturn(employeeDto);

        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeCreateDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("12345"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).createEmployee(any(EmployeeCreateDto.class));
    }

    @Test
    void testGetEmployeeById() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(employeeDto);

        mockMvc.perform(get("/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("12345"))
                .andExpect(jsonPath("$.name").value("John Doe"));

        verify(employeeService, times(1)).getEmployeeById(1L);
    }

    @Test
    void testDeleteEmployee() throws Exception {
        doNothing().when(employeeService).deleteEmployee(1L);

        mockMvc.perform(delete("/employees/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        verify(employeeService, times(1)).deleteEmployee(1L);
    }
}
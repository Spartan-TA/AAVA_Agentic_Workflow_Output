package SpringBootTestSuite;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.*;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDTO validEmployeeDTO;
    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setId(1L);
        validEmployeeDTO.setName("John Doe");
        validEmployeeDTO.setBadgeId("EMP001");
        validEmployeeDTO.setRole(Role.WORKER);
        validEmployeeDTO.setDepartment("Logistics");
        validEmployeeDTO.setHireDate(LocalDate.of(2023, 1, 1));
        validEmployeeDTO.setStatus(Status.ACTIVE);
    }

    @Test
    public void testCreateEmployee_ValidRequest_Returns201() throws Exception {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    public void testCreateEmployee_InvalidRequest_Returns400() throws Exception {
        EmployeeDTO invalidDTO = new EmployeeDTO();
        invalidDTO.setBadgeId(""); // Invalid badgeId

        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void testGetEmployee_ValidId_Returns200() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(validEmployeeDTO);

        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.badgeId").value("EMP001"));
    }

    @Test
    public void testGetEmployee_InvalidId_Returns404() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenThrow(new EntityNotFoundException("Employee not found"));

        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    public void testGetAllEmployees_ReturnsPagedList() throws Exception {
        Page<EmployeeDTO> page = new PageImpl<>(Collections.singletonList(validEmployeeDTO));
        when(employeeService.getAllEmployees(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("John Doe"));
    }

    @Test
    public void testUpdateEmployee_ValidRequest_Returns200() throws Exception {
        validEmployeeDTO.setName("Jane Doe");
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(validEmployeeDTO);

        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validEmployeeDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane Doe"));
    }

    @Test
    public void testDeleteEmployee_ValidId_Returns204() throws Exception {
        doNothing().when(employeeService).softDeleteEmployee(1L);

        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }
}
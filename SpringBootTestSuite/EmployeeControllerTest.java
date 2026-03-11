package SpringBootTestSuite;

import com.warehouse.modules.employee.controller.EmployeeController;
import com.warehouse.modules.employee.dto.EmployeeDto;
import com.warehouse.modules.employee.enums.EmployeeRole;
import com.warehouse.modules.employee.enums.EmployeeStatus;
import com.warehouse.modules.employee.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.time.LocalDate;
import java.util.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @MockBean
    private EmployeeService employeeService;

    // Minimal stubs for missing classes/enums
    enum EmployeeRole { ADMIN, HR, SUPERVISOR, WORKER }
    enum EmployeeStatus { ACTIVE, TERMINATED }
    static class EmployeeDto {
        Long id;
        String badgeId;
        String name;
        EmployeeRole role;
        String department;
        String shiftGroup;
        LocalDate hireDate;
        EmployeeStatus status;
        public static Builder builder() { return new Builder(); }
        static class Builder {
            EmployeeDto dto = new EmployeeDto();
            public Builder id(Long id) { dto.id = id; return this; }
            public Builder badgeId(String b) { dto.badgeId = b; return this; }
            public Builder name(String n) { dto.name = n; return this; }
            public Builder role(EmployeeRole r) { dto.role = r; return this; }
            public Builder department(String d) { dto.department = d; return this; }
            public Builder shiftGroup(String s) { dto.shiftGroup = s; return this; }
            public Builder hireDate(LocalDate h) { dto.hireDate = h; return this; }
            public Builder status(EmployeeStatus s) { dto.status = s; return this; }
            public EmployeeDto build() { return dto; }
        }
        public Long getId() { return id; }
        public String getBadgeId() { return badgeId; }
        public String getName() { return name; }
        public EmployeeRole getRole() { return role; }
        public String getDepartment() { return department; }
        public String getShiftGroup() { return shiftGroup; }
        public LocalDate getHireDate() { return hireDate; }
        public EmployeeStatus getStatus() { return status; }
    }

    EmployeeDto employeeDto = EmployeeDto.builder()
            .id(1L)
            .badgeId("B123")
            .name("John Doe")
            .role(EmployeeRole.ADMIN)
            .department("HR")
            .shiftGroup("A")
            .hireDate(LocalDate.of(2020,1,1))
            .status(EmployeeStatus.ACTIVE)
            .build();

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(new EmployeeController()).build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("getAllEmployees returns page of EmployeeDto for authorized roles")
    void getAllEmployees_authorized() throws Exception {
        Page<EmployeeDto> page = new PageImpl<>(List.of(employeeDto));
        when(employeeService.getAllEmployees(0, 20)).thenReturn(page);
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    @DisplayName("getAllEmployees forbidden for unauthorized role")
    void getAllEmployees_forbidden() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("getEmployeeById returns EmployeeDto if found")
    void getEmployeeById_found() throws Exception {
        when(employeeService.getEmployeeById(1L)).thenReturn(Optional.of(employeeDto));
        mockMvc.perform(get("/api/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("B123")));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("getEmployeeById returns 404 if not found")
    void getEmployeeById_notFound() throws Exception {
        when(employeeService.getEmployeeById(2L)).thenReturn(Optional.empty());
        mockMvc.perform(get("/api/employees/2"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("createEmployee returns 201 and EmployeeDto")
    void createEmployee_normal() throws Exception {
        EmployeeDto dto = employeeDto;
        when(employeeService.createEmployee(any(), eq("pass"))).thenReturn(dto);
        String json = "{" +
                ""badgeId":"B123"," +
                ""name":"John Doe"," +
                ""role":"ADMIN"," +
                ""department":"HR"," +
                ""shiftGroup":"A"," +
                ""hireDate":"2020-01-01"}";
        mockMvc.perform(post("/api/employees?password=pass")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId", is("B123")));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    @DisplayName("createEmployee forbidden for non-admin")
    void createEmployee_forbidden() throws Exception {
        String json = "{" +
                ""badgeId":"B123"," +
                ""name":"John Doe"," +
                ""role":"ADMIN"," +
                ""department":"HR"," +
                ""shiftGroup":"A"," +
                ""hireDate":"2020-01-01"}";
        mockMvc.perform(post("/api/employees?password=pass")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("updateEmployee returns updated EmployeeDto")
    void updateEmployee_normal() throws Exception {
        EmployeeDto updated = EmployeeDto.builder().id(1L).name("Jane").build();
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(updated);
        String json = "{" +
                ""name":"Jane"}";
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane")));
    }

    @Test
    @WithMockUser(roles = {"HR"})
    @DisplayName("updateEmployee allowed for HR")
    void updateEmployee_hr() throws Exception {
        EmployeeDto updated = EmployeeDto.builder().id(1L).name("Jane").build();
        when(employeeService.updateEmployee(eq(1L), any())).thenReturn(updated);
        String json = "{" +
                ""name":"Jane"}";
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = {"WORKER"})
    @DisplayName("updateEmployee forbidden for worker")
    void updateEmployee_forbidden() throws Exception {
        String json = "{" +
                ""name":"Jane"}";
        mockMvc.perform(put("/api/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("softDeleteEmployee returns 204")
    void softDeleteEmployee_normal() throws Exception {
        doNothing().when(employeeService).softDeleteEmployee(1L);
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = {"HR"})
    @DisplayName("softDeleteEmployee forbidden for HR")
    void softDeleteEmployee_forbidden() throws Exception {
        mockMvc.perform(delete("/api/employees/1"))
                .andExpect(status().isForbidden());
    }
}

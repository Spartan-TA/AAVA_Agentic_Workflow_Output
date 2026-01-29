package SpringBootTestSuite;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.warehouse.employee_mgmt.controller.EmployeeController;
import com.warehouse.employee_mgmt.dto.EmployeeDto;
import com.warehouse.employee_mgmt.exception.DuplicateResourceException;
import com.warehouse.employee_mgmt.exception.NotFoundException;
import com.warehouse.employee_mgmt.service.EmployeeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

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

    private EmployeeDto employeeDto;
    private UUID employeeId;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        employeeId = UUID.randomUUID();
        pageable = PageRequest.of(0, 10);
        employeeDto = EmployeeDto.builder()
                .id(employeeId)
                .name("John Doe")
                .badgeId("BADGE123")
                .role("WORKER")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now().minusDays(1))
                .status("ACTIVE")
                .tenantId(UUID.randomUUID())
                .build();
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testList_NormalCase_ReturnsPage")
    void testList_NormalCase_ReturnsPage() throws Exception {
        Page<EmployeeDto> page = new PageImpl<>(List.of(employeeDto));
        when(employeeService.getAll(any(), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].name", is("John Doe")));
        verify(employeeService).getAll(any(), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testList_WithSearch_ReturnsFilteredPage")
    void testList_WithSearch_ReturnsFilteredPage() throws Exception {
        Page<EmployeeDto> page = new PageImpl<>(List.of(employeeDto));
        when(employeeService.getAll(eq("John"), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees").param("search", "John"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name", is("John Doe")));
        verify(employeeService).getAll(eq("John"), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testGet_NormalCase_ReturnsEmployeeDto")
    void testGet_NormalCase_ReturnsEmployeeDto() throws Exception {
        when(employeeService.getById(employeeId)).thenReturn(employeeDto);
        mockMvc.perform(get("/api/employees/{id}", employeeId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("John Doe")));
        verify(employeeService).getById(employeeId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testGet_NotFound_Throws404")
    void testGet_NotFound_Throws404() throws Exception {
        when(employeeService.getById(employeeId)).thenThrow(new NotFoundException("Employee not found"));
        mockMvc.perform(get("/api/employees/{id}", employeeId))
                .andExpect(status().isNotFound());
        verify(employeeService).getById(employeeId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testGetByBadgeId_NormalCase_ReturnsEmployeeDto")
    void testGetByBadgeId_NormalCase_ReturnsEmployeeDto() throws Exception {
        when(employeeService.getByBadgeId("BADGE123")).thenReturn(employeeDto);
        mockMvc.perform(get("/api/employees/badge/{badgeId}", "BADGE123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId", is("BADGE123")));
        verify(employeeService).getByBadgeId("BADGE123");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testGetByBadgeId_NotFound_Throws404")
    void testGetByBadgeId_NotFound_Throws404() throws Exception {
        when(employeeService.getByBadgeId("BADGE123")).thenThrow(new NotFoundException("Employee not found"));
        mockMvc.perform(get("/api/employees/badge/{badgeId}", "BADGE123"))
                .andExpect(status().isNotFound());
        verify(employeeService).getByBadgeId("BADGE123");
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testCreate_NormalCase_ReturnsCreated")
    void testCreate_NormalCase_ReturnsCreated() throws Exception {
        when(employeeService.create(any(EmployeeDto.class))).thenReturn(employeeDto);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is("John Doe")));
        verify(employeeService).create(any(EmployeeDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testCreate_DuplicateBadgeId_Throws409")
    void testCreate_DuplicateBadgeId_Throws409() throws Exception {
        when(employeeService.create(any(EmployeeDto.class))).thenThrow(new DuplicateResourceException("Badge ID exists"));
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDto)))
                .andExpect(status().isConflict());
        verify(employeeService).create(any(EmployeeDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testUpdate_NormalCase_ReturnsUpdatedDto")
    void testUpdate_NormalCase_ReturnsUpdatedDto() throws Exception {
        EmployeeDto updatedDto = employeeDto.toBuilder().name("Jane Doe").build();
        when(employeeService.update(eq(employeeId), any(EmployeeDto.class))).thenReturn(updatedDto);
        mockMvc.perform(put("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Jane Doe")));
        verify(employeeService).update(eq(employeeId), any(EmployeeDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testUpdate_NotFound_Throws404")
    void testUpdate_NotFound_Throws404() throws Exception {
        when(employeeService.update(eq(employeeId), any(EmployeeDto.class))).thenThrow(new NotFoundException("Not found"));
        mockMvc.perform(put("/api/employees/{id}", employeeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(employeeDto)))
                .andExpect(status().isNotFound());
        verify(employeeService).update(eq(employeeId), any(EmployeeDto.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testDelete_NormalCase_ReturnsNoContent")
    void testDelete_NormalCase_ReturnsNoContent() throws Exception {
        doNothing().when(employeeService).softDelete(employeeId);
        mockMvc.perform(delete("/api/employees/{id}", employeeId))
                .andExpect(status().isNoContent());
        verify(employeeService).softDelete(employeeId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testDelete_NotFound_Throws404")
    void testDelete_NotFound_Throws404() throws Exception {
        doThrow(new NotFoundException("Not found")).when(employeeService).softDelete(employeeId);
        mockMvc.perform(delete("/api/employees/{id}", employeeId))
                .andExpect(status().isNotFound());
        verify(employeeService).softDelete(employeeId);
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testGetByDepartment_NormalCase_ReturnsPage")
    void testGetByDepartment_NormalCase_ReturnsPage() throws Exception {
        Page<EmployeeDto> page = new PageImpl<>(List.of(employeeDto));
        when(employeeService.getByDepartment(eq("Logistics"), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees/department/{department}", "Logistics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department", is("Logistics")));
        verify(employeeService).getByDepartment(eq("Logistics"), any(Pageable.class));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testGetByStatus_NormalCase_ReturnsPage")
    void testGetByStatus_NormalCase_ReturnsPage() throws Exception {
        Page<EmployeeDto> page = new PageImpl<>(List.of(employeeDto));
        when(employeeService.getByStatus(eq("ACTIVE"), any(Pageable.class))).thenReturn(page);
        mockMvc.perform(get("/api/employees/status/{status}", "ACTIVE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status", is("ACTIVE")));
        verify(employeeService).getByStatus(eq("ACTIVE"), any(Pageable.class));
    }

    // Boundary and edge cases
    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testCreate_Boundary_MaxLengthFields_Success")
    void testCreate_Boundary_MaxLengthFields_Success() throws Exception {
        EmployeeDto maxDto = EmployeeDto.builder()
                .name("A".repeat(100))
                .badgeId("B".repeat(50))
                .role("ADMIN")
                .department("D".repeat(50))
                .shiftGroup("S".repeat(50))
                .hireDate(LocalDate.now())
                .status("ACTIVE")
                .tenantId(UUID.randomUUID())
                .build();
        when(employeeService.create(any(EmployeeDto.class))).thenReturn(maxDto);
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(maxDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name", is(maxDto.getName())));
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testCreate_Boundary_EmptyStrings_ValidationError")
    void testCreate_Boundary_EmptyStrings_ValidationError() throws Exception {
        EmployeeDto emptyDto = employeeDto.toBuilder().name("").badgeId("").role("").status("").build();
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(emptyDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = {"ADMIN"})
    @DisplayName("testCreate_EdgeCase_FutureHireDate_ValidationError")
    void testCreate_EdgeCase_FutureHireDate_ValidationError() throws Exception {
        EmployeeDto futureDto = employeeDto.toBuilder().hireDate(LocalDate.now().plusDays(1)).build();
        mockMvc.perform(post("/api/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(futureDto)))
                .andExpect(status().isBadRequest());
    }
}

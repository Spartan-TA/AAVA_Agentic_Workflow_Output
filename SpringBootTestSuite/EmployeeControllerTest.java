package SpringBootTestSuite;

import com.example.warehouse.controller.EmployeeController;
import com.example.warehouse.dto.EmployeeDto;
import com.example.warehouse.service.EmployeeService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.*;

import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDto validDto;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        validDto = new EmployeeDto();
        validDto.setName("Alice Smith");
        validDto.setBadgeId("ABCD1234");
        validDto.setRole("Worker");
        validDto.setDepartment("Logistics");
        validDto.setShiftGroup("A");
        validDto.setHireDate(LocalDate.now().minusDays(1));
        validDto.setStatus("ACTIVE");
        validDto.setEmail("alice.smith@example.com");
        validDto.setPhone("+12345678901");
    }

    @Test
    void testPostEmployees_Valid_Returns201() throws Exception {
        when(employeeService.create(any(EmployeeDto.class))).thenReturn(validDto);
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    void testPostEmployees_Invalid_Returns400() throws Exception {
        EmployeeDto invalidDto = new EmployeeDto();
        invalidDto.setName("");
        invalidDto.setBadgeId("bad#id");
        invalidDto.setHireDate(LocalDate.now().plusDays(1));
        invalidDto.setStatus("");
        when(employeeService.create(any(EmployeeDto.class))).thenThrow(new IllegalArgumentException("Invalid input"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testGetEmployees_IdExists_Returns200() throws Exception {
        when(employeeService.get(1L)).thenReturn(validDto);
        mockMvc.perform(get("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Smith"));
    }

    @Test
    void testGetEmployees_IdNotExists_Returns404() throws Exception {
        when(employeeService.get(99L)).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetEmployees_PaginatedList_Returns200() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeDto> page = new PageImpl<>(Arrays.asList(validDto));
        when(employeeService.list(pageable)).thenReturn(page);
        mockMvc.perform(get("/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alice Smith"));
    }

    @Test
    void testGetEmployees_PaginationEmpty_Returns200() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeDto> page = new PageImpl<>(Collections.emptyList());
        when(employeeService.list(pageable)).thenReturn(page);
        mockMvc.perform(get("/employees?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void testSearchByName_ExactMatch_Returns200() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeDto> page = new PageImpl<>(Arrays.asList(validDto));
        when(employeeService.searchByName("Alice Smith", pageable)).thenReturn(page);
        mockMvc.perform(get("/employees/search?name=Alice Smith&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("Alice Smith"));
    }

    @Test
    void testSearchByName_NoMatch_Returns200() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeDto> page = new PageImpl<>(Collections.emptyList());
        when(employeeService.searchByName("Zachary", pageable)).thenReturn(page);
        mockMvc.perform(get("/employees/search?name=Zachary&page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty());
    }

    @Test
    void testGetEmployeesByDepartment_Valid_Returns200() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        Page<EmployeeDto> page = new PageImpl<>(Arrays.asList(validDto));
        when(employeeService.listByDepartment("Logistics", pageable)).thenReturn(page);
        mockMvc.perform(get("/employees/department/Logistics?page=0&size=10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].department").value("Logistics"));
    }

    @Test
    void testGetEmployeesByBadgeId_Exists_Returns200() throws Exception {
        when(employeeService.getByBadgeId("ABCD1234")).thenReturn(validDto);
        mockMvc.perform(get("/employees/badge/ABCD1234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("ABCD1234"));
    }

    @Test
    void testGetEmployeesByBadgeId_NotExists_Returns404() throws Exception {
        when(employeeService.getByBadgeId("ZZZZ9999")).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(get("/employees/badge/ZZZZ9999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testPutEmployees_Valid_Returns200() throws Exception {
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setName("Alice Updated");
        updateDto.setBadgeId("ABCD1234");
        updateDto.setHireDate(LocalDate.now().minusDays(1));
        updateDto.setStatus("ACTIVE");
        when(employeeService.update(eq(1L), any(EmployeeDto.class))).thenReturn(updateDto);
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Alice Updated"));
    }

    @Test
    void testPutEmployees_NotExists_Returns404() throws Exception {
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setName("Alice Updated");
        updateDto.setBadgeId("ABCD1234");
        updateDto.setHireDate(LocalDate.now().minusDays(1));
        updateDto.setStatus("ACTIVE");
        when(employeeService.update(eq(99L), any(EmployeeDto.class))).thenThrow(new IllegalArgumentException("Not found"));
        mockMvc.perform(put("/employees/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testDeleteEmployees_Exists_Returns204() throws Exception {
        doNothing().when(employeeService).delete(1L);
        mockMvc.perform(delete("/employees/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteEmployees_NotExists_Returns404() throws Exception {
        doThrow(new IllegalArgumentException("Not found")).when(employeeService).delete(99L);
        mockMvc.perform(delete("/employees/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetDepartmentCount_Valid_Returns200() throws Exception {
        when(employeeService.countActiveByDepartment("Logistics")).thenReturn(2L);
        mockMvc.perform(get("/employees/department/Logistics/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void testGetDepartmentCount_NoActive_Returns200() throws Exception {
        when(employeeService.countActiveByDepartment("Finance")).thenReturn(0L);
        mockMvc.perform(get("/employees/department/Finance/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void testPostEmployees_NullInput_Returns400() throws Exception {
        when(employeeService.create(any())).thenThrow(new IllegalArgumentException("Null input"));
        mockMvc.perform(post("/employees")
                .contentType(MediaType.APPLICATION_JSON)
                .content("null"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testPutEmployees_InvalidInput_Returns400() throws Exception {
        EmployeeDto invalidDto = new EmployeeDto();
        invalidDto.setName("");
        invalidDto.setBadgeId("bad#id");
        invalidDto.setHireDate(LocalDate.now().plusDays(1));
        invalidDto.setStatus("");
        when(employeeService.update(eq(1L), any(EmployeeDto.class))).thenThrow(new IllegalArgumentException("Invalid input"));
        mockMvc.perform(put("/employees/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isBadRequest());
    }
}

package com.company.warehouse.employee;

import com.company.warehouse.employee.dto.EmployeeDto;
import com.company.warehouse.employee.entity.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class EmployeeIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Employee employee;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
        employee = Employee.builder()
                .badgeId("B123456")
                .name("John Doe")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .deleted(false)
                .build();
        employee = employeeRepository.save(employee);
    }

    @Test
    @DisplayName("GET /api/employees returns all not deleted employees")
    void getAllEmployees_returnsAllNotDeletedEmployees() throws Exception {
        mockMvc.perform(get("/api/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].badgeId").value("B123456"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} returns employee when exists")
    void getEmployeeById_exists_returnsEmployee() throws Exception {
        mockMvc.perform(get("/api/employees/" + employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.badgeId").value("B123456"));
    }

    @Test
    @DisplayName("GET /api/employees/{id} returns 404 when not exists")
    void getEmployeeById_notExists_returns404() throws Exception {
        mockMvc.perform(get("/api/employees/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/employees creates employee when valid")
    void createEmployee_valid_createsEmployee() throws Exception {
        EmployeeDto dto = EmployeeDto.builder()
                .badgeId("B654321")
                .name("Jane Smith")
                .role("Manager")
                .department("HR")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("ACTIVE")
                .build();
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.badgeId").value("B654321"));
        assertThat(employeeRepository.existsByBadgeIdAndDeletedFalse("B654321")).isTrue();
    }

    @Test
    @DisplayName("POST /api/employees returns 400 when badgeId exists")
    void createEmployee_badgeIdExists_returns400() throws Exception {
        EmployeeDto dto = EmployeeDto.builder()
                .badgeId("B123456")
                .name("Jane Smith")
                .role("Manager")
                .department("HR")
                .shiftGroup("B")
                .hireDate(LocalDate.of(2021, 2, 2))
                .status("ACTIVE")
                .build();
        mockMvc.perform(post("/api/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/employees/{id} updates employee when valid")
    void updateEmployee_valid_updatesEmployee() throws Exception {
        EmployeeDto updateDto = EmployeeDto.builder()
                .id(employee.getId())
                .badgeId("B123456")
                .name("John Updated")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
        mockMvc.perform(put("/api/employees/" + employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("John Updated"));
        Optional<Employee> updated = employeeRepository.findById(employee.getId());
        assertThat(updated).isPresent();
        assertThat(updated.get().getName()).isEqualTo("John Updated");
    }

    @Test
    @DisplayName("PUT /api/employees/{id} returns 404 when not exists")
    void updateEmployee_notExists_returns404() throws Exception {
        EmployeeDto updateDto = EmployeeDto.builder()
                .id(99999L)
                .badgeId("B999999")
                .name("Ghost")
                .role("Worker")
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.of(2020, 1, 1))
                .status("ACTIVE")
                .build();
        mockMvc.perform(put("/api/employees/99999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateDto)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} marks employee as deleted")
    void deleteEmployee_marksEmployeeAsDeleted() throws Exception {
        mockMvc.perform(delete("/api/employees/" + employee.getId()))
                .andExpect(status().isNoContent());
        Optional<Employee> deleted = employeeRepository.findById(employee.getId());
        assertThat(deleted).isPresent();
        assertThat(deleted.get().getDeleted()).isTrue();
    }

    @Test
    @DisplayName("DELETE /api/employees/{id} returns 404 when not exists")
    void deleteEmployee_notExists_returns404() throws Exception {
        mockMvc.perform(delete("/api/employees/99999"))
                .andExpect(status().isNotFound());
    }
}

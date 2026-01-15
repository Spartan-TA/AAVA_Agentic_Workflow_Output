package com.warehouse.ems.employee.mapper;

import com.warehouse.ems.employee.dto.EmployeeDTO;
import com.warehouse.ems.employee.entity.Employee;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Comprehensive JUnit test suite for EmployeeMapper
 * Tests cover entity-to-DTO and DTO-to-entity conversions with all field mappings
 */
class EmployeeMapperTest {

    private EmployeeMapper employeeMapper;
    private Employee testEmployee;
    private EmployeeDTO testEmployeeDTO;

    @BeforeEach
    void setUp() {
        employeeMapper = Mappers.getMapper(EmployeeMapper.class);

        testEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();

        testEmployeeDTO = EmployeeDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role("WORKER")
                .department("Warehouse")
                .shiftGroup("Day Shift")
                .hireDate(LocalDate.of(2023, 1, 15))
                .status("ACTIVE")
                .build();
    }

    // ========== ENTITY TO DTO TESTS ==========

    @Test
    void testToDTO_ValidEntity_Success() {
        // Act
        EmployeeDTO result = employeeMapper.toDTO(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee.getId(), result.getId());
        assertEquals(testEmployee.getName(), result.getName());
        assertEquals(testEmployee.getBadgeId(), result.getBadgeId());
        assertEquals(testEmployee.getRole(), result.getRole());
        assertEquals(testEmployee.getDepartment(), result.getDepartment());
        assertEquals(testEmployee.getShiftGroup(), result.getShiftGroup());
        assertEquals(testEmployee.getHireDate(), result.getHireDate());
        assertEquals(testEmployee.getStatus(), result.getStatus());
    }

    @Test
    void testToDTO_NullEntity_ReturnsNull() {
        // Act
        EmployeeDTO result = employeeMapper.toDTO(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToDTO_EntityWithNullFields_Success() {
        // Arrange
        Employee entityWithNulls = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role(null)
                .department(null)
                .shiftGroup(null)
                .hireDate(null)
                .status("ACTIVE")
                .build();

        // Act
        EmployeeDTO result = employeeMapper.toDTO(entityWithNulls);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertNull(result.getRole());
        assertNull(result.getDepartment());
        assertNull(result.getShiftGroup());
        assertNull(result.getHireDate());
    }

    @Test
    void testToDTO_EntityWithMinimalFields_Success() {
        // Arrange
        Employee minimalEntity = Employee.builder()
                .name("Minimal Employee")
                .badgeId("EMP999")
                .status("ACTIVE")
                .build();

        // Act
        EmployeeDTO result = employeeMapper.toDTO(minimalEntity);

        // Assert
        assertNotNull(result);
        assertEquals("Minimal Employee", result.getName());
        assertEquals("EMP999", result.getBadgeId());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    void testToDTO_EntityWithLongName_Success() {
        // Arrange
        String longName = "A".repeat(255);
        testEmployee.setName(longName);

        // Act
        EmployeeDTO result = employeeMapper.toDTO(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(longName, result.getName());
    }

    @Test
    void testToDTO_EntityWithSpecialCharacters_Success() {
        // Arrange
        testEmployee.setName("John O'Doe-Smith");
        testEmployee.setDepartment("Warehouse & Logistics");

        // Act
        EmployeeDTO result = employeeMapper.toDTO(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("John O'Doe-Smith", result.getName());
        assertEquals("Warehouse & Logistics", result.getDepartment());
    }

    // ========== DTO TO ENTITY TESTS ==========

    @Test
    void testToEntity_ValidDTO_Success() {
        // Act
        Employee result = employeeMapper.toEntity(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployeeDTO.getId(), result.getId());
        assertEquals(testEmployeeDTO.getName(), result.getName());
        assertEquals(testEmployeeDTO.getBadgeId(), result.getBadgeId());
        assertEquals(testEmployeeDTO.getRole(), result.getRole());
        assertEquals(testEmployeeDTO.getDepartment(), result.getDepartment());
        assertEquals(testEmployeeDTO.getShiftGroup(), result.getShiftGroup());
        assertEquals(testEmployeeDTO.getHireDate(), result.getHireDate());
        assertEquals(testEmployeeDTO.getStatus(), result.getStatus());
    }

    @Test
    void testToEntity_NullDTO_ReturnsNull() {
        // Act
        Employee result = employeeMapper.toEntity(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToEntity_DTOWithNullFields_Success() {
        // Arrange
        EmployeeDTO dtoWithNulls = EmployeeDTO.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("EMP001")
                .role(null)
                .department(null)
                .shiftGroup(null)
                .hireDate(null)
                .status("ACTIVE")
                .build();

        // Act
        Employee result = employeeMapper.toEntity(dtoWithNulls);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
        assertNull(result.getRole());
        assertNull(result.getDepartment());
        assertNull(result.getShiftGroup());
        assertNull(result.getHireDate());
    }

    @Test
    void testToEntity_DTOWithMinimalFields_Success() {
        // Arrange
        EmployeeDTO minimalDTO = EmployeeDTO.builder()
                .name("Minimal Employee")
                .badgeId("EMP999")
                .status("ACTIVE")
                .build();

        // Act
        Employee result = employeeMapper.toEntity(minimalDTO);

        // Assert
        assertNotNull(result);
        assertEquals("Minimal Employee", result.getName());
        assertEquals("EMP999", result.getBadgeId());
        assertEquals("ACTIVE", result.getStatus());
    }

    @Test
    void testToEntity_DTOWithAllRoles_Success() {
        // Test all valid roles
        String[] roles = {"ADMIN", "HR", "SUPERVISOR", "WORKER"};

        for (String role : roles) {
            // Arrange
            testEmployeeDTO.setRole(role);

            // Act
            Employee result = employeeMapper.toEntity(testEmployeeDTO);

            // Assert
            assertNotNull(result);
            assertEquals(role, result.getRole());
        }
    }

    @Test
    void testToEntity_DTOWithAllStatuses_Success() {
        // Test all valid statuses
        String[] statuses = {"ACTIVE", "INACTIVE", "ON_LEAVE", "TERMINATED"};

        for (String status : statuses) {
            // Arrange
            testEmployeeDTO.setStatus(status);

            // Act
            Employee result = employeeMapper.toEntity(testEmployeeDTO);

            // Assert
            assertNotNull(result);
            assertEquals(status, result.getStatus());
        }
    }

    // ========== LIST CONVERSION TESTS ==========

    @Test
    void testToDTOList_ValidEntityList_Success() {
        // Arrange
        Employee employee2 = Employee.builder()
                .id(2L)
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .status("ACTIVE")
                .build();

        List<Employee> entities = Arrays.asList(testEmployee, employee2);

        // Act
        List<EmployeeDTO> result = employeeMapper.toDTOList(entities);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
    }

    @Test
    void testToDTOList_EmptyList_ReturnsEmptyList() {
        // Arrange
        List<Employee> emptyList = Arrays.asList();

        // Act
        List<EmployeeDTO> result = employeeMapper.toDTOList(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToDTOList_NullList_ReturnsNull() {
        // Act
        List<EmployeeDTO> result = employeeMapper.toDTOList(null);

        // Assert
        assertNull(result);
    }

    @Test
    void testToEntityList_ValidDTOList_Success() {
        // Arrange
        EmployeeDTO dto2 = EmployeeDTO.builder()
                .id(2L)
                .name("Jane Smith")
                .badgeId("EMP002")
                .role("SUPERVISOR")
                .department("Logistics")
                .status("ACTIVE")
                .build();

        List<EmployeeDTO> dtos = Arrays.asList(testEmployeeDTO, dto2);

        // Act
        List<Employee> result = employeeMapper.toEntityList(dtos);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("John Doe", result.get(0).getName());
        assertEquals("Jane Smith", result.get(1).getName());
    }

    @Test
    void testToEntityList_EmptyList_ReturnsEmptyList() {
        // Arrange
        List<EmployeeDTO> emptyList = Arrays.asList();

        // Act
        List<Employee> result = employeeMapper.toEntityList(emptyList);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testToEntityList_NullList_ReturnsNull() {
        // Act
        List<Employee> result = employeeMapper.toEntityList(null);

        // Assert
        assertNull(result);
    }

    // ========== BIDIRECTIONAL CONVERSION TESTS ==========

    @Test
    void testBidirectionalConversion_EntityToDTOToEntity_Success() {
        // Act
        EmployeeDTO dto = employeeMapper.toDTO(testEmployee);
        Employee entity = employeeMapper.toEntity(dto);

        // Assert
        assertNotNull(entity);
        assertEquals(testEmployee.getId(), entity.getId());
        assertEquals(testEmployee.getName(), entity.getName());
        assertEquals(testEmployee.getBadgeId(), entity.getBadgeId());
        assertEquals(testEmployee.getRole(), entity.getRole());
        assertEquals(testEmployee.getDepartment(), entity.getDepartment());
        assertEquals(testEmployee.getShiftGroup(), entity.getShiftGroup());
        assertEquals(testEmployee.getHireDate(), entity.getHireDate());
        assertEquals(testEmployee.getStatus(), entity.getStatus());
    }

    @Test
    void testBidirectionalConversion_DTOToEntityToDTO_Success() {
        // Act
        Employee entity = employeeMapper.toEntity(testEmployeeDTO);
        EmployeeDTO dto = employeeMapper.toDTO(entity);

        // Assert
        assertNotNull(dto);
        assertEquals(testEmployeeDTO.getId(), dto.getId());
        assertEquals(testEmployeeDTO.getName(), dto.getName());
        assertEquals(testEmployeeDTO.getBadgeId(), dto.getBadgeId());
        assertEquals(testEmployeeDTO.getRole(), dto.getRole());
        assertEquals(testEmployeeDTO.getDepartment(), dto.getDepartment());
        assertEquals(testEmployeeDTO.getShiftGroup(), dto.getShiftGroup());
        assertEquals(testEmployeeDTO.getHireDate(), dto.getHireDate());
        assertEquals(testEmployeeDTO.getStatus(), dto.getStatus());
    }

    // ========== EDGE CASE TESTS ==========

    @Test
    void testToDTO_EntityWithFutureHireDate_Success() {
        // Arrange
        testEmployee.setHireDate(LocalDate.now().plusDays(30));

        // Act
        EmployeeDTO result = employeeMapper.toDTO(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee.getHireDate(), result.getHireDate());
    }

    @Test
    void testToDTO_EntityWithPastHireDate_Success() {
        // Arrange
        testEmployee.setHireDate(LocalDate.of(1990, 1, 1));

        // Act
        EmployeeDTO result = employeeMapper.toDTO(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(testEmployee.getHireDate(), result.getHireDate());
    }

    @Test
    void testToEntity_DTOWithEmptyStrings_Success() {
        // Arrange
        testEmployeeDTO.setDepartment("");
        testEmployeeDTO.setShiftGroup("");

        // Act
        Employee result = employeeMapper.toEntity(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getDepartment());
        assertEquals("", result.getShiftGroup());
    }

    @Test
    void testToDTO_EntityWithWhitespaceFields_Success() {
        // Arrange
        testEmployee.setDepartment("   Warehouse   ");
        testEmployee.setShiftGroup("  Day Shift  ");

        // Act
        EmployeeDTO result = employeeMapper.toDTO(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals("   Warehouse   ", result.getDepartment());
        assertEquals("  Day Shift  ", result.getShiftGroup());
    }

    @Test
    void testToEntity_DTOWithZeroId_Success() {
        // Arrange
        testEmployeeDTO.setId(0L);

        // Act
        Employee result = employeeMapper.toEntity(testEmployeeDTO);

        // Assert
        assertNotNull(result);
        assertEquals(0L, result.getId());
    }

    @Test
    void testToDTO_EntityWithMaxLongId_Success() {
        // Arrange
        testEmployee.setId(Long.MAX_VALUE);

        // Act
        EmployeeDTO result = employeeMapper.toDTO(testEmployee);

        // Assert
        assertNotNull(result);
        assertEquals(Long.MAX_VALUE, result.getId());
    }
}
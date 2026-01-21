package com.wms.employee;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

/**
 * JUnit tests for EmployeeMapper covering entity-DTO conversions and edge cases.
 */
public class EmployeeMapperTest {

    private Employee validEmployee;
    private EmployeeDTO validEmployeeDTO;

    @BeforeEach
    public void setUp() {
        validEmployee = new Employee();
        validEmployee.setId(1L);
        validEmployee.setName("Alice");
        validEmployee.setBadgeId("BADGE789");
        validEmployee.setRole("HR");
        validEmployee.setDepartment("Admin");
        validEmployee.setShiftGroup("C");
        validEmployee.setHireDate(LocalDate.of(2020, 1, 1));
        validEmployee.setStatus("ACTIVE");

        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setName("Alice");
        validEmployeeDTO.setBadgeId("BADGE789");
        validEmployeeDTO.setRole("HR");
        validEmployeeDTO.setDepartment("Admin");
        validEmployeeDTO.setShiftGroup("C");
        validEmployeeDTO.setHireDate(LocalDate.of(2020, 1, 1));
        validEmployeeDTO.setStatus("ACTIVE");
    }

    @Test
    public void testToDTO_ValidEntity_ReturnsDTO() {
        EmployeeDTO dto = EmployeeMapper.toDTO(validEmployee);
        assertNotNull(dto);
        assertEquals("Alice", dto.getName());
        assertEquals("BADGE789", dto.getBadgeId());
    }

    @Test
    public void testToDTO_NullEntity_ReturnsNull() {
        EmployeeDTO dto = EmployeeMapper.toDTO(null);
        assertNull(dto);
    }

    @Test
    public void testToDTO_EntityWithNullFields_ReturnsDTOWithNulls() {
        Employee emp = new Employee();
        emp.setId(2L);
        EmployeeDTO dto = EmployeeMapper.toDTO(emp);
        assertNotNull(dto);
        assertNull(dto.getName());
        assertNull(dto.getBadgeId());
    }

    @Test
    public void testToEntity_ValidDTO_ReturnsEntity() {
        Employee emp = EmployeeMapper.toEntity(validEmployeeDTO);
        assertNotNull(emp);
        assertEquals("Alice", emp.getName());
        assertEquals("BADGE789", emp.getBadgeId());
    }

    @Test
    public void testToEntity_NullDTO_ReturnsNull() {
        Employee emp = EmployeeMapper.toEntity(null);
        assertNull(emp);
    }

    @Test
    public void testToEntity_DTOWithNullFields_ReturnsEntityWithNulls() {
        EmployeeDTO dto = new EmployeeDTO();
        Employee emp = EmployeeMapper.toEntity(dto);
        assertNotNull(emp);
        assertNull(emp.getName());
        assertNull(emp.getBadgeId());
    }

    @Test
    public void testToDTO_EntityWithEmptyStrings_ReturnsDTOWithEmptyStrings() {
        Employee emp = new Employee();
        emp.setName("");
        emp.setBadgeId("");
        EmployeeDTO dto = EmployeeMapper.toDTO(emp);
        assertNotNull(dto);
        assertEquals("", dto.getName());
        assertEquals("", dto.getBadgeId());
    }

    @Test
    public void testToEntity_DTOWithEmptyStrings_ReturnsEntityWithEmptyStrings() {
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("");
        dto.setBadgeId("");
        Employee emp = EmployeeMapper.toEntity(dto);
        assertNotNull(emp);
        assertEquals("", emp.getName());
        assertEquals("", emp.getBadgeId());
    }

    @Test
    public void testToDTO_EntityWithAllFields_ReturnsDTOWithAllFields() {
        EmployeeDTO dto = EmployeeMapper.toDTO(validEmployee);
        assertEquals(validEmployee.getName(), dto.getName());
        assertEquals(validEmployee.getBadgeId(), dto.getBadgeId());
        assertEquals(validEmployee.getRole(), dto.getRole());
        assertEquals(validEmployee.getDepartment(), dto.getDepartment());
        assertEquals(validEmployee.getShiftGroup(), dto.getShiftGroup());
        assertEquals(validEmployee.getHireDate(), dto.getHireDate());
        assertEquals(validEmployee.getStatus(), dto.getStatus());
    }

    @Test
    public void testToEntity_DTOWithAllFields_ReturnsEntityWithAllFields() {
        Employee emp = EmployeeMapper.toEntity(validEmployeeDTO);
        assertEquals(validEmployeeDTO.getName(), emp.getName());
        assertEquals(validEmployeeDTO.getBadgeId(), emp.getBadgeId());
        assertEquals(validEmployeeDTO.getRole(), emp.getRole());
        assertEquals(validEmployeeDTO.getDepartment(), emp.getDepartment());
        assertEquals(validEmployeeDTO.getShiftGroup(), emp.getShiftGroup());
        assertEquals(validEmployeeDTO.getHireDate(), emp.getHireDate());
        assertEquals(validEmployeeDTO.getStatus(), emp.getStatus());
    }
}

package SpringBootTestSuite;

import com.wms.ems.employee.dto.EmployeeDTO;
import com.wms.ems.employee.mapper.EmployeeMapper;
import com.wms.ems.employee.model.Employee;
import com.wms.ems.employee.model.Role;
import com.wms.ems.employee.model.Status;
import com.wms.ems.employee.repository.EmployeeRepository;
import com.wms.ems.employee.service.impl.EmployeeServiceImpl;
import com.wms.ems.exception.BadRequestException;
import com.wms.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {
    @Mock EmployeeRepository employeeRepository;
    @Mock EmployeeMapper employeeMapper;
    @InjectMocks EmployeeServiceImpl employeeService;

    EmployeeDTO validDto;
    Employee validEmployee;

    @BeforeEach
    void setup() {
        validDto = EmployeeDTO.builder()
                .name("John Doe")
                .badgeId("BADGE123")
                .role(Role.WORKER)
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status(Status.ACTIVE)
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .build();
        validEmployee = Employee.builder()
                .id(1L)
                .name("John Doe")
                .badgeId("BADGE123")
                .role(Role.WORKER)
                .department("Logistics")
                .shiftGroup("A")
                .hireDate(LocalDate.now())
                .status(Status.ACTIVE)
                .email("john.doe@example.com")
                .phoneNumber("1234567890")
                .deleted(false)
                .build();
    }

    @Test
    @DisplayName("Create employee with valid input")
    void testCreateValid() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE123")).thenReturn(false);
        when(employeeMapper.toEntity(validDto)).thenReturn(validEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toDto(validEmployee)).thenReturn(validDto);
        EmployeeDTO result = employeeService.create(validDto);
        assertEquals(validDto.getBadgeId(), result.getBadgeId());
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Create employee with duplicate badge ID throws BadRequestException")
    void testCreateDuplicateBadgeId() {
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE123")).thenReturn(true);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> employeeService.create(validDto));
        assertTrue(ex.getMessage().contains("Badge ID already exists"));
    }

    @Test
    @DisplayName("Create employee with null badge ID throws BadRequestException")
    void testCreateNullBadgeId() {
        EmployeeDTO dto = EmployeeDTO.builder().badgeId(null).build();
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(null)).thenReturn(false);
        when(employeeMapper.toEntity(dto)).thenThrow(new NullPointerException());
        assertThrows(NullPointerException.class, () -> employeeService.create(dto));
    }

    @Test
    @DisplayName("Update employee with valid input")
    void testUpdateValid() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE123")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toDto(validEmployee)).thenReturn(validDto);
        EmployeeDTO result = employeeService.update(1L, validDto);
        assertEquals(validDto.getBadgeId(), result.getBadgeId());
    }

    @Test
    @DisplayName("Update employee with deleted status throws BadRequestException")
    void testUpdateDeletedEmployee() {
        Employee deletedEmployee = validEmployee;
        deletedEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(deletedEmployee));
        BadRequestException ex = assertThrows(BadRequestException.class, () -> employeeService.update(1L, validDto));
        assertTrue(ex.getMessage().contains("Cannot update deleted employee"));
    }

    @Test
    @DisplayName("Update employee with duplicate badge ID throws BadRequestException")
    void testUpdateDuplicateBadgeId() {
        Employee employee = validEmployee;
        employee.setBadgeId("BADGE999");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE123")).thenReturn(true);
        BadRequestException ex = assertThrows(BadRequestException.class, () -> employeeService.update(1L, validDto));
        assertTrue(ex.getMessage().contains("Badge ID already exists"));
    }

    @Test
    @DisplayName("Update employee not found throws ResourceNotFoundException")
    void testUpdateNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> employeeService.update(1L, validDto));
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    @DisplayName("Delete employee with valid input")
    void testDeleteValid() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        assertDoesNotThrow(() -> employeeService.delete(1L));
        verify(employeeRepository).save(any(Employee.class));
    }

    @Test
    @DisplayName("Delete employee not found throws ResourceNotFoundException")
    void testDeleteNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> employeeService.delete(1L));
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    @DisplayName("Find employee by ID with valid input")
    void testFindByIdValid() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployee));
        when(employeeMapper.toDto(validEmployee)).thenReturn(validDto);
        EmployeeDTO result = employeeService.findById(1L);
        assertEquals(validDto.getBadgeId(), result.getBadgeId());
    }

    @Test
    @DisplayName("Find employee by ID not found throws ResourceNotFoundException")
    void testFindByIdNotFound() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.empty());
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> employeeService.findById(1L));
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    @DisplayName("Find employee by ID deleted throws ResourceNotFoundException")
    void testFindByIdDeleted() {
        Employee deletedEmployee = validEmployee;
        deletedEmployee.setDeleted(true);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(deletedEmployee));
        ResourceNotFoundException ex = assertThrows(ResourceNotFoundException.class, () -> employeeService.findById(1L));
        assertTrue(ex.getMessage().contains("Employee not found"));
    }

    @Test
    @DisplayName("Create employee with empty name throws exception")
    void testCreateEmptyName() {
        EmployeeDTO dto = EmployeeDTO.builder().name("").badgeId("BADGE124").role(Role.WORKER).hireDate(LocalDate.now()).status(Status.ACTIVE).build();
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE124")).thenReturn(false);
        when(employeeMapper.toEntity(dto)).thenThrow(new IllegalArgumentException("Employee name is required"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    @DisplayName("Create employee with boundary badge ID length")
    void testCreateBoundaryBadgeIdLength() {
        String badgeId = "B".repeat(50);
        EmployeeDTO dto = EmployeeDTO.builder().name("Jane Doe").badgeId(badgeId).role(Role.WORKER).hireDate(LocalDate.now()).status(Status.ACTIVE).build();
        when(employeeRepository.existsByBadgeIdAndDeletedFalse(badgeId)).thenReturn(false);
        when(employeeMapper.toEntity(dto)).thenReturn(validEmployee);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEmployee);
        when(employeeMapper.toDto(validEmployee)).thenReturn(dto);
        EmployeeDTO result = employeeService.create(dto);
        assertEquals(badgeId, result.getBadgeId());
    }

    @Test
    @DisplayName("Create employee with invalid role throws exception")
    void testCreateInvalidRole() {
        EmployeeDTO dto = EmployeeDTO.builder().name("Jane Doe").badgeId("BADGE125").role(null).hireDate(LocalDate.now()).status(Status.ACTIVE).build();
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE125")).thenReturn(false);
        when(employeeMapper.toEntity(dto)).thenThrow(new IllegalArgumentException("Role is required"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    @DisplayName("Create employee with null hire date throws exception")
    void testCreateNullHireDate() {
        EmployeeDTO dto = EmployeeDTO.builder().name("Jane Doe").badgeId("BADGE126").role(Role.WORKER).hireDate(null).status(Status.ACTIVE).build();
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE126")).thenReturn(false);
        when(employeeMapper.toEntity(dto)).thenThrow(new IllegalArgumentException("Hire date is required"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    @DisplayName("Create employee with null status throws exception")
    void testCreateNullStatus() {
        EmployeeDTO dto = EmployeeDTO.builder().name("Jane Doe").badgeId("BADGE127").role(Role.WORKER).hireDate(LocalDate.now()).status(null).build();
        when(employeeRepository.existsByBadgeIdAndDeletedFalse("BADGE127")).thenReturn(false);
        when(employeeMapper.toEntity(dto)).thenThrow(new IllegalArgumentException("Status is required"));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }
}

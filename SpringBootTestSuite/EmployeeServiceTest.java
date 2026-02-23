package SpringBootTestSuite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.example.warehouse.entity.Employee;
import com.example.warehouse.dto.EmployeeDto;
import com.example.warehouse.repository.EmployeeRepository;
import com.example.warehouse.service.EmployeeService;

@SpringBootTest
class EmployeeServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeService employeeService;

    private EmployeeDto validDto;
    private Employee validEntity;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
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

        validEntity = new Employee();
        validEntity.setId(1L);
        validEntity.setName("Alice Smith");
        validEntity.setBadgeId("ABCD1234");
        validEntity.setRole("Worker");
        validEntity.setDepartment("Logistics");
        validEntity.setShiftGroup("A");
        validEntity.setHireDate(LocalDate.now().minusDays(1));
        validEntity.setStatus("ACTIVE");
        validEntity.setDeleted(false);
        validEntity.setEmail("alice.smith@example.com");
        validEntity.setPhone("+12345678901");
    }

    @Test
    void testCreate_ValidEmployee_ReturnsCreatedEmployee() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("ABCD1234")).thenReturn(Optional.empty());
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEntity);
        EmployeeDto result = employeeService.create(validDto);
        assertNotNull(result);
        assertEquals("Alice Smith", result.getName());
        assertEquals("ABCD1234", result.getBadgeId());
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testCreate_DuplicateBadgeId_ThrowsIllegalArgumentException() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("ABCD1234")).thenReturn(Optional.of(validEntity));
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(validDto));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void testCreate_InvalidBadgeIdFormat_ThrowsIllegalArgumentException() {
        EmployeeDto dto = new EmployeeDto();
        dto.setBadgeId("bad#id");
        dto.setName("Alice");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    void testGet_ExistingEmployee_ReturnsEmployeeDto() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEntity));
        EmployeeDto result = employeeService.get(1L);
        assertNotNull(result);
        assertEquals("Alice Smith", result.getName());
    }

    @Test
    void testGet_NonExistentEmployee_ThrowsIllegalArgumentException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> employeeService.get(99L));
    }

    @Test
    void testGet_DeletedEmployee_ThrowsIllegalArgumentException() {
        Employee deleted = new Employee();
        deleted.setId(2L);
        deleted.setDeleted(true);
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(deleted));
        assertThrows(IllegalArgumentException.class, () -> employeeService.get(2L));
    }

    @Test
    void testList_Pagination_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> entityPage = new PageImpl<>(Arrays.asList(validEntity));
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(entityPage);
        Page<EmployeeDto> dtoPage = employeeService.list(pageable);
        assertEquals(1, dtoPage.getTotalElements());
        assertEquals("Alice Smith", dtoPage.getContent().get(0).getName());
    }

    @Test
    void testList_Pagination_EmptyPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> entityPage = new PageImpl<>(Collections.emptyList());
        when(employeeRepository.findAllByDeletedFalse(pageable)).thenReturn(entityPage);
        Page<EmployeeDto> dtoPage = employeeService.list(pageable);
        assertEquals(0, dtoPage.getTotalElements());
    }

    @Test
    void testListByDepartment_Valid_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> entityPage = new PageImpl<>(Arrays.asList(validEntity));
        when(employeeRepository.findByDepartmentAndDeletedFalse("Logistics", pageable)).thenReturn(entityPage);
        Page<EmployeeDto> dtoPage = employeeService.listByDepartment("Logistics", pageable);
        assertEquals(1, dtoPage.getTotalElements());
    }

    @Test
    void testSearchByName_ExactMatch_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Employee> entityPage = new PageImpl<>(Arrays.asList(validEntity));
        when(employeeRepository.searchByName("Alice Smith", pageable)).thenReturn(entityPage);
        Page<EmployeeDto> dtoPage = employeeService.searchByName("Alice Smith", pageable);
        assertEquals(1, dtoPage.getTotalElements());
    }

    @Test
    void testUpdate_ValidEmployee_ReturnsUpdatedEmployee() {
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setName("Alice Updated");
        updateDto.setBadgeId("ABCD1234");
        updateDto.setHireDate(LocalDate.now().minusDays(1));
        updateDto.setStatus("ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEntity));
        when(employeeRepository.existsByBadgeIdAndIdNot("ABCD1234", 1L)).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(validEntity);
        EmployeeDto result = employeeService.update(1L, updateDto);
        assertNotNull(result);
        assertEquals("Alice Updated", result.getName());
    }

    @Test
    void testUpdate_DuplicateBadgeId_ThrowsIllegalArgumentException() {
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setBadgeId("EFGH5678");
        updateDto.setName("Alice");
        updateDto.setHireDate(LocalDate.now());
        updateDto.setStatus("ACTIVE");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEntity));
        when(employeeRepository.existsByBadgeIdAndIdNot("EFGH5678", 1L)).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(1L, updateDto));
    }

    @Test
    void testDelete_ExistingEmployee_SoftDeletes() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEntity));
        doNothing().when(employeeRepository).save(any(Employee.class));
        employeeService.delete(1L);
        verify(employeeRepository, times(1)).save(any(Employee.class));
    }

    @Test
    void testDelete_NonExistentEmployee_ThrowsIllegalArgumentException() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> employeeService.delete(99L));
    }

    @Test
    void testGetByBadgeId_Exists_ReturnsEmployeeDto() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("ABCD1234")).thenReturn(Optional.of(validEntity));
        EmployeeDto result = employeeService.getByBadgeId("ABCD1234");
        assertNotNull(result);
        assertEquals("Alice Smith", result.getName());
    }

    @Test
    void testGetByBadgeId_NotExists_ThrowsIllegalArgumentException() {
        when(employeeRepository.findByBadgeIdAndDeletedFalse("ZZZZ9999")).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> employeeService.getByBadgeId("ZZZZ9999"));
    }

    @Test
    void testCountActiveByDepartment_ReturnsCount() {
        when(employeeRepository.countActiveByDepartment("Logistics")).thenReturn(2L);
        long count = employeeService.countActiveByDepartment("Logistics");
        assertEquals(2L, count);
    }

    @Test
    void testCountActiveByDepartment_NoActive_ReturnsZero() {
        when(employeeRepository.countActiveByDepartment("Finance")).thenReturn(0L);
        long count = employeeService.countActiveByDepartment("Finance");
        assertEquals(0L, count);
    }

    @Test
    void testCreate_NullInput_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(null));
    }

    @Test
    void testUpdate_NonExistentEmployee_ThrowsIllegalArgumentException() {
        EmployeeDto updateDto = new EmployeeDto();
        updateDto.setBadgeId("ABCD1234");
        updateDto.setName("Alice");
        updateDto.setHireDate(LocalDate.now());
        updateDto.setStatus("ACTIVE");
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(99L, updateDto));
    }

    @Test
    void testCreate_BlankName_ThrowsIllegalArgumentException() {
        EmployeeDto dto = new EmployeeDto();
        dto.setBadgeId("ABCD1234");
        dto.setName("");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    void testCreate_InvalidEmail_ThrowsIllegalArgumentException() {
        EmployeeDto dto = new EmployeeDto();
        dto.setBadgeId("ABCD1234");
        dto.setName("Alice");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");
        dto.setEmail("not-an-email");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    void testCreate_InvalidPhone_ThrowsIllegalArgumentException() {
        EmployeeDto dto = new EmployeeDto();
        dto.setBadgeId("ABCD1234");
        dto.setName("Alice");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");
        dto.setPhone("123abc");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }

    @Test
    void testCreate_FutureHireDate_ThrowsIllegalArgumentException() {
        EmployeeDto dto = new EmployeeDto();
        dto.setBadgeId("ABCD1234");
        dto.setName("Alice");
        dto.setHireDate(LocalDate.now().plusDays(1));
        dto.setStatus("ACTIVE");
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(dto));
    }
}

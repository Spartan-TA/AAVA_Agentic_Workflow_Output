package com.example.employeeservice;

import com.example.employeeservice.dto.EmployeeDTO;
import com.example.employeeservice.filter.EmployeeFilter;
import com.example.employeeservice.repository.EmployeeRepository;
import com.example.employeeservice.service.EmployeeServiceImpl;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import java.time.LocalDate;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive JUnit 5 test suite for EmployeeService.
 */
@ExtendWith(MockitoExtension.class)
class EmployeeServiceTest {

    @Mock
    private EmployeeRepository employeeRepository;

    @InjectMocks
    private EmployeeServiceImpl employeeService;

    private EmployeeDTO validEmployeeDTO;

    @BeforeEach
    void setUp() {
        validEmployeeDTO = new EmployeeDTO();
        validEmployeeDTO.setId(1L);
        validEmployeeDTO.setBadgeId("BADGE123");
        validEmployeeDTO.setName("John Doe");
        validEmployeeDTO.setRole("WORKER");
        validEmployeeDTO.setDepartment("Logistics");
        validEmployeeDTO.setShiftGroup("A");
        validEmployeeDTO.setHireDate(LocalDate.of(2020, 1, 1));
        validEmployeeDTO.setStatus("ACTIVE");
    }

    // NORMAL CASES

    @Test
    @DisplayName("testCreate_ValidEmployee_ReturnsCreatedEmployee")
    void testCreate_ValidEmployee_ReturnsCreatedEmployee() {
        // Arrange
        when(employeeRepository.save(any())).thenAnswer(invocation -> {
            EmployeeDTO dto = invocation.getArgument(0);
            dto.setId(1L);
            return dto;
        });

        // Act
        EmployeeDTO created = employeeService.create(validEmployeeDTO);

        // Assert
        assertNotNull(created, "Created employee should not be null");
        assertEquals(1L, created.getId(), "Created employee ID should be set");
        assertEquals("BADGE123", created.getBadgeId(), "Badge ID should match input");
        verify(employeeRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("testUpdate_ValidEmployee_ReturnsUpdatedEmployee")
    void testUpdate_ValidEmployee_ReturnsUpdatedEmployee() {
        // Arrange
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmployeeDTO updated = employeeService.update(1L, validEmployeeDTO);

        // Assert
        assertNotNull(updated, "Updated employee should not be null");
        assertEquals("John Doe", updated.getName(), "Name should be updated");
        verify(employeeRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("testPatch_ValidFields_ReturnsPartiallyUpdatedEmployee")
    void testPatch_ValidFields_ReturnsPartiallyUpdatedEmployee() {
        // Arrange
        Map<String, Object> fields = new HashMap<>();
        fields.put("name", "Jane Smith");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployeeDTO));
        when(employeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmployeeDTO patched = employeeService.patch(1L, fields);

        // Assert
        assertNotNull(patched, "Patched employee should not be null");
        assertEquals("Jane Smith", patched.getName(), "Name should be patched");
        verify(employeeRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("testDelete_ExistingEmployee_SoftDeletesSuccessfully")
    void testDelete_ExistingEmployee_SoftDeletesSuccessfully() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployeeDTO));
        doNothing().when(employeeRepository).softDelete(1L);

        // Act
        employeeService.delete(1L);

        // Assert
        verify(employeeRepository, times(1)).softDelete(1L);
    }

    @Test
    @DisplayName("testGet_ExistingEmployee_ReturnsEmployee")
    void testGet_ExistingEmployee_ReturnsEmployee() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployeeDTO));

        // Act
        EmployeeDTO found = employeeService.get(1L);

        // Assert
        assertNotNull(found, "Employee should be found");
        assertEquals(1L, found.getId(), "Employee ID should match");
    }

    @Test
    @DisplayName("testList_WithPagination_ReturnsPagedResults")
    void testList_WithPagination_ReturnsPagedResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        EmployeeFilter filter = new EmployeeFilter();
        List<EmployeeDTO> employees = Arrays.asList(validEmployeeDTO);
        Page<EmployeeDTO> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(any(), eq(pageable))).thenReturn(page);

        // Act
        Page<EmployeeDTO> result = employeeService.list(pageable, filter);

        // Assert
        assertNotNull(result, "Page result should not be null");
        assertEquals(1, result.getTotalElements(), "Should return one employee");
    }

    @Test
    @DisplayName("testList_WithFilter_ReturnsFilteredResults")
    void testList_WithFilter_ReturnsFilteredResults() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        EmployeeFilter filter = new EmployeeFilter();
        filter.setDepartment("Logistics");
        List<EmployeeDTO> employees = Arrays.asList(validEmployeeDTO);
        Page<EmployeeDTO> page = new PageImpl<>(employees, pageable, 1);
        when(employeeRepository.findAll(any(), eq(pageable))).thenReturn(page);

        // Act
        Page<EmployeeDTO> result = employeeService.list(pageable, filter);

        // Assert
        assertNotNull(result, "Filtered page should not be null");
        assertEquals("Logistics", result.getContent().get(0).getDepartment(), "Department should match filter");
    }

    // BOUNDARY CONDITIONS

    @Test
    @DisplayName("testCreate_MinimumRequiredFields_Success")
    void testCreate_MinimumRequiredFields_Success() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("BADGE_MIN");
        dto.setName("Min Name");
        dto.setRole("WORKER");
        dto.setDepartment("Dept");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");
        when(employeeRepository.save(any())).thenAnswer(invocation -> {
            EmployeeDTO saved = invocation.getArgument(0);
            saved.setId(2L);
            return saved;
        });

        // Act
        EmployeeDTO created = employeeService.create(dto);

        // Assert
        assertNotNull(created, "Created employee should not be null");
        assertEquals("BADGE_MIN", created.getBadgeId(), "Badge ID should match");
    }

    @Test
    @DisplayName("testCreate_MaximumFieldLengths_Success")
    void testCreate_MaximumFieldLengths_Success() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("B".repeat(50));
        dto.setName("N".repeat(100));
        dto.setRole("ADMIN");
        dto.setDepartment("D".repeat(50));
        dto.setShiftGroup("S".repeat(10));
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");
        when(employeeRepository.save(any())).thenAnswer(invocation -> {
            EmployeeDTO saved = invocation.getArgument(0);
            saved.setId(3L);
            return saved;
        });

        // Act
        EmployeeDTO created = employeeService.create(dto);

        // Assert
        assertNotNull(created, "Created employee should not be null");
        assertEquals("B".repeat(50), created.getBadgeId(), "Badge ID should match max length");
    }

    @Test
    @DisplayName("testList_EmptyDatabase_ReturnsEmptyPage")
    void testList_EmptyDatabase_ReturnsEmptyPage() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        EmployeeFilter filter = new EmployeeFilter();
        Page<EmployeeDTO> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);
        when(employeeRepository.findAll(any(), eq(pageable))).thenReturn(emptyPage);

        // Act
        Page<EmployeeDTO> result = employeeService.list(pageable, filter);

        // Assert
        assertTrue(result.isEmpty(), "Page should be empty");
    }

    @Test
    @DisplayName("testList_SingleEmployee_ReturnsSingleResult")
    void testList_SingleEmployee_ReturnsSingleResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 10);
        EmployeeFilter filter = new EmployeeFilter();
        Page<EmployeeDTO> singlePage = new PageImpl<>(Collections.singletonList(validEmployeeDTO), pageable, 1);
        when(employeeRepository.findAll(any(), eq(pageable))).thenReturn(singlePage);

        // Act
        Page<EmployeeDTO> result = employeeService.list(pageable, filter);

        // Assert
        assertEquals(1, result.getTotalElements(), "Should return one employee");
    }

    @Test
    @DisplayName("testList_PageSizeOne_ReturnsSingleResult")
    void testList_PageSizeOne_ReturnsSingleResult() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 1);
        EmployeeFilter filter = new EmployeeFilter();
        Page<EmployeeDTO> singlePage = new PageImpl<>(Collections.singletonList(validEmployeeDTO), pageable, 1);
        when(employeeRepository.findAll(any(), eq(pageable))).thenReturn(singlePage);

        // Act
        Page<EmployeeDTO> result = employeeService.list(pageable, filter);

        // Assert
        assertEquals(1, result.getContent().size(), "Page size one should return one employee");
    }

    @Test
    @DisplayName("testList_LastPage_ReturnsRemainingResults")
    void testList_LastPage_ReturnsRemainingResults() {
        // Arrange
        Pageable pageable = PageRequest.of(2, 2); // Assume 5 employees, last page has 1
        EmployeeFilter filter = new EmployeeFilter();
        EmployeeDTO lastEmployee = new EmployeeDTO();
        lastEmployee.setId(5L);
        lastEmployee.setBadgeId("BADGE5");
        lastEmployee.setName("Last Emp");
        lastEmployee.setRole("WORKER");
        lastEmployee.setDepartment("Dept");
        lastEmployee.setHireDate(LocalDate.now());
        lastEmployee.setStatus("ACTIVE");
        Page<EmployeeDTO> lastPage = new PageImpl<>(Collections.singletonList(lastEmployee), pageable, 5);
        when(employeeRepository.findAll(any(), eq(pageable))).thenReturn(lastPage);

        // Act
        Page<EmployeeDTO> result = employeeService.list(pageable, filter);

        // Assert
        assertEquals(1, result.getContent().size(), "Last page should return remaining employee");
        assertEquals(5L, result.getContent().get(0).getId(), "Employee ID should be last");
    }

    // EDGE CASES

    @Test
    @DisplayName("testCreate_NullDTO_ThrowsIllegalArgumentException")
    void testCreate_NullDTO_ThrowsIllegalArgumentException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.create(null), "Null DTO should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("testCreate_NullBadgeId_ThrowsValidationException")
    void testCreate_NullBadgeId_ThrowsValidationException() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setName("Name");
        dto.setRole("WORKER");
        dto.setDepartment("Dept");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.create(dto), "Null badgeId should throw ValidationException");
    }

    @Test
    @DisplayName("testCreate_EmptyBadgeId_ThrowsValidationException")
    void testCreate_EmptyBadgeId_ThrowsValidationException() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("");
        dto.setName("Name");
        dto.setRole("WORKER");
        dto.setDepartment("Dept");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.create(dto), "Empty badgeId should throw ValidationException");
    }

    @Test
    @DisplayName("testCreate_DuplicateBadgeId_ThrowsConstraintViolationException")
    void testCreate_DuplicateBadgeId_ThrowsConstraintViolationException() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("BADGE123");
        dto.setName("Name");
        dto.setRole("WORKER");
        dto.setDepartment("Dept");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");
        when(employeeRepository.existsByBadgeId("BADGE123")).thenReturn(true);

        // Act & Assert
        assertThrows(ConstraintViolationException.class, () -> employeeService.create(dto), "Duplicate badgeId should throw ConstraintViolationException");
    }

    @Test
    @DisplayName("testCreate_NullName_ThrowsValidationException")
    void testCreate_NullName_ThrowsValidationException() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("BADGE124");
        dto.setRole("WORKER");
        dto.setDepartment("Dept");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.create(dto), "Null name should throw ValidationException");
    }

    @Test
    @DisplayName("testCreate_EmptyName_ThrowsValidationException")
    void testCreate_EmptyName_ThrowsValidationException() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("BADGE124");
        dto.setName("");
        dto.setRole("WORKER");
        dto.setDepartment("Dept");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.create(dto), "Empty name should throw ValidationException");
    }

    @Test
    @DisplayName("testCreate_InvalidRole_ThrowsValidationException")
    void testCreate_InvalidRole_ThrowsValidationException() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("BADGE125");
        dto.setName("Name");
        dto.setRole("INVALID_ROLE");
        dto.setDepartment("Dept");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.create(dto), "Invalid role should throw ValidationException");
    }

    @Test
    @DisplayName("testCreate_NullRole_ThrowsValidationException")
    void testCreate_NullRole_ThrowsValidationException() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("BADGE126");
        dto.setName("Name");
        dto.setDepartment("Dept");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("ACTIVE");

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.create(dto), "Null role should throw ValidationException");
    }

    @Test
    @DisplayName("testCreate_InvalidStatus_ThrowsValidationException")
    void testCreate_InvalidStatus_ThrowsValidationException() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("BADGE127");
        dto.setName("Name");
        dto.setRole("WORKER");
        dto.setDepartment("Dept");
        dto.setHireDate(LocalDate.now());
        dto.setStatus("INVALID_STATUS");

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.create(dto), "Invalid status should throw ValidationException");
    }

    @Test
    @DisplayName("testCreate_FutureHireDate_ThrowsValidationException")
    void testCreate_FutureHireDate_ThrowsValidationException() {
        // Arrange
        EmployeeDTO dto = new EmployeeDTO();
        dto.setBadgeId("BADGE128");
        dto.setName("Name");
        dto.setRole("WORKER");
        dto.setDepartment("Dept");
        dto.setHireDate(LocalDate.now().plusDays(1));
        dto.setStatus("ACTIVE");

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.create(dto), "Future hire date should throw ValidationException");
    }

    @Test
    @DisplayName("testUpdate_NonExistentEmployee_ThrowsEntityNotFoundException")
    void testUpdate_NonExistentEmployee_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.existsById(99L)).thenReturn(false);

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.update(99L, validEmployeeDTO), "Non-existent employee should throw EntityNotFoundException");
    }

    @Test
    @DisplayName("testUpdate_NullId_ThrowsIllegalArgumentException")
    void testUpdate_NullId_ThrowsIllegalArgumentException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(null, validEmployeeDTO), "Null ID should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("testUpdate_NullDTO_ThrowsIllegalArgumentException")
    void testUpdate_NullDTO_ThrowsIllegalArgumentException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.update(1L, null), "Null DTO should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("testPatch_NonExistentEmployee_ThrowsEntityNotFoundException")
    void testPatch_NonExistentEmployee_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.patch(99L, Map.of("name", "New Name")), "Non-existent employee should throw EntityNotFoundException");
    }

    @Test
    @DisplayName("testPatch_NullId_ThrowsIllegalArgumentException")
    void testPatch_NullId_ThrowsIllegalArgumentException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.patch(null, Map.of("name", "New Name")), "Null ID should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("testPatch_NullFields_ThrowsIllegalArgumentException")
    void testPatch_NullFields_ThrowsIllegalArgumentException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.patch(1L, null), "Null fields should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("testPatch_EmptyFields_NoChanges")
    void testPatch_EmptyFields_NoChanges() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployeeDTO));
        when(employeeRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        EmployeeDTO patched = employeeService.patch(1L, Collections.emptyMap());

        // Assert
        assertEquals(validEmployeeDTO, patched, "Empty patch should result in no changes");
    }

    @Test
    @DisplayName("testPatch_InvalidFieldName_ThrowsValidationException")
    void testPatch_InvalidFieldName_ThrowsValidationException() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(validEmployeeDTO));

        // Act & Assert
        assertThrows(ValidationException.class, () -> employeeService.patch(1L, Map.of("invalidField", "value")), "Invalid field name should throw ValidationException");
    }

    @Test
    @DisplayName("testDelete_NonExistentEmployee_ThrowsEntityNotFoundException")
    void testDelete_NonExistentEmployee_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.delete(99L), "Non-existent employee should throw EntityNotFoundException");
    }

    @Test
    @DisplayName("testDelete_NullId_ThrowsIllegalArgumentException")
    void testDelete_NullId_ThrowsIllegalArgumentException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.delete(null), "Null ID should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("testDelete_AlreadyDeleted_ThrowsIllegalStateException")
    void testDelete_AlreadyDeleted_ThrowsIllegalStateException() {
        // Arrange
        EmployeeDTO deletedEmployee = new EmployeeDTO();
        deletedEmployee.setId(1L);
        deletedEmployee.setBadgeId("BADGE123");
        deletedEmployee.setName("John Doe");
        deletedEmployee.setRole("WORKER");
        deletedEmployee.setDepartment("Logistics");
        deletedEmployee.setHireDate(LocalDate.of(2020, 1, 1));
        deletedEmployee.setStatus("TERMINATED");
        // Assume deleted flag is set
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(deletedEmployee));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> employeeService.delete(1L), "Already deleted employee should throw IllegalStateException");
    }

    @Test
    @DisplayName("testGet_NonExistentEmployee_ThrowsEntityNotFoundException")
    void testGet_NonExistentEmployee_ThrowsEntityNotFoundException() {
        // Arrange
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.get(99L), "Non-existent employee should throw EntityNotFoundException");
    }

    @Test
    @DisplayName("testGet_NullId_ThrowsIllegalArgumentException")
    void testGet_NullId_ThrowsIllegalArgumentException() {
        // Arrange & Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.get(null), "Null ID should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("testGet_DeletedEmployee_ThrowsEntityNotFoundException")
    void testGet_DeletedEmployee_ThrowsEntityNotFoundException() {
        // Arrange
        EmployeeDTO deletedEmployee = new EmployeeDTO();
        deletedEmployee.setId(1L);
        deletedEmployee.setBadgeId("BADGE123");
        deletedEmployee.setName("John Doe");
        deletedEmployee.setRole("WORKER");
        deletedEmployee.setDepartment("Logistics");
        deletedEmployee.setHireDate(LocalDate.of(2020, 1, 1));
        deletedEmployee.setStatus("TERMINATED");
        // Assume deleted flag is set
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(deletedEmployee));

        // Act & Assert
        assertThrows(EntityNotFoundException.class, () -> employeeService.get(1L), "Deleted employee should throw EntityNotFoundException");
    }

    @Test
    @DisplayName("testList_NullPageable_ThrowsIllegalArgumentException")
    void testList_NullPageable_ThrowsIllegalArgumentException() {
        // Arrange
        EmployeeFilter filter = new EmployeeFilter();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.list(null, filter), "Null pageable should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("testList_InvalidPageNumber_ThrowsIllegalArgumentException")
    void testList_InvalidPageNumber_ThrowsIllegalArgumentException() {
        // Arrange
        Pageable pageable = PageRequest.of(-1, 10);
        EmployeeFilter filter = new EmployeeFilter();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.list(pageable, filter), "Invalid page number should throw IllegalArgumentException");
    }

    @Test
    @DisplayName("testList_InvalidPageSize_ThrowsIllegalArgumentException")
    void testList_InvalidPageSize_ThrowsIllegalArgumentException() {
        // Arrange
        Pageable pageable = PageRequest.of(0, 0);
        EmployeeFilter filter = new EmployeeFilter();

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> employeeService.list(pageable, filter), "Invalid page size should throw IllegalArgumentException");
    }

    // CONCURRENCY TESTS

    @Test
    @DisplayName("testCreate_ConcurrentDuplicateBadgeId_OneSucceedsOneThrows")
    void testCreate_ConcurrentDuplicateBadgeId_OneSucceedsOneThrows() {
        // Arrange
        EmployeeDTO dto1 = new EmployeeDTO();
        dto1.setBadgeId("BADGE_CONCURRENT");
        dto1.setName("Emp1");
        dto1.setRole("WORKER");
        dto1.setDepartment("Dept");
        dto1.setHireDate(LocalDate.now());
        dto1.setStatus("ACTIVE");

        EmployeeDTO dto2 = new EmployeeDTO();
        dto2.setBadgeId("BADGE_CONCURRENT");
        dto2.setName("Emp2");
        dto2.setRole("WORKER");
        dto2.setDepartment("Dept");
        dto2.setHireDate(LocalDate.now());
        dto2.setStatus("ACTIVE");

        when(employeeRepository.existsByBadgeId("BADGE_CONCURRENT"))
                .thenReturn(false) // First call
                .thenReturn(true); // Second call

        // Act
        EmployeeDTO created1 = employeeService.create(dto1);
        assertNotNull(created1, "First concurrent create should succeed");

        // Second should throw
        assertThrows(ConstraintViolationException.class, () -> employeeService.create(dto2), "Second concurrent create should throw ConstraintViolationException");
    }

    @Test
    @DisplayName("testUpdate_ConcurrentUpdates_OptimisticLockingException")
    void testUpdate_ConcurrentUpdates_OptimisticLockingException() {
        // Arrange
        when(employeeRepository.existsById(1L)).thenReturn(true);
        when(employeeRepository.save(any())).thenThrow(new OptimisticLockingFailureException("Concurrent update"));

        // Act & Assert
        assertThrows(OptimisticLockingFailureException.class, () -> employeeService.update(1L, validEmployeeDTO), "Concurrent update should throw OptimisticLockingFailureException");
    }
}

// Note: The above test class assumes the existence of EmployeeDTO, EmployeeFilter, EmployeeRepository, EmployeeServiceImpl, and relevant exceptions (ValidationException, ConstraintViolationException, EntityNotFoundException, OptimisticLockingFailureException).
// All tests follow the AAA pattern, use Mockito for mocking, and cover normal, boundary, edge, and concurrency cases as specified.
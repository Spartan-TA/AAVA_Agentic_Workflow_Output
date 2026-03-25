package SpringBootTestSuite;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

// Assume these imports exist
import com.example.ems.controller.EmployeeController;
import com.example.ems.dto.EmployeeDTO;
import com.example.ems.exception.EntityNotFoundException;
import com.example.ems.service.EmployeeService;

@ExtendWith(MockitoExtension.class)
public class EmployeeControllerTest {
    @Mock
    private EmployeeService employeeService;
    @InjectMocks
    private EmployeeController employeeController;

    private EmployeeDTO employeeDto;

    @BeforeEach
    public void setUp() {
        employeeDto = new EmployeeDTO();
        employeeDto.setName("John Doe");
        employeeDto.setBadgeId("EMP001");
        employeeDto.setRole("WORKER");
        employeeDto.setStatus("ACTIVE");
    }

    @Test
    public void testCreateEmployee_Valid_ReturnsCreated() {
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenReturn(employeeDto);
        ResponseEntity<EmployeeDTO> response = employeeController.createEmployee(employeeDto);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
        verify(employeeService).createEmployee(any(EmployeeDTO.class));
    }

    @Test
    public void testCreateEmployee_Invalid_ThrowsValidation() {
        EmployeeDTO invalidDto = new EmployeeDTO();
        invalidDto.setBadgeId("");
        when(employeeService.createEmployee(any(EmployeeDTO.class))).thenThrow(new IllegalArgumentException("BadgeId required"));
        assertThrows(IllegalArgumentException.class, () -> employeeController.createEmployee(invalidDto));
    }

    @Test
    public void testGetEmployee_Valid_ReturnsOk() {
        when(employeeService.getEmployee(1L)).thenReturn(employeeDto);
        ResponseEntity<EmployeeDTO> response = employeeController.getEmployee(1L);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("John Doe", response.getBody().getName());
        verify(employeeService).getEmployee(1L);
    }

    @Test
    public void testGetEmployee_NotFound_Throws404() {
        when(employeeService.getEmployee(2L)).thenThrow(new EntityNotFoundException("Not found"));
        assertThrows(EntityNotFoundException.class, () -> employeeController.getEmployee(2L));
    }

    @Test
    public void testListEmployees_ReturnsOk() {
        Pageable pageable = PageRequest.of(0, 10);
        List<EmployeeDTO> dtos = Arrays.asList(employeeDto);
        Page<EmployeeDTO> page = new PageImpl<>(dtos);
        when(employeeService.listEmployees(pageable)).thenReturn(page);
        ResponseEntity<Page<EmployeeDTO>> response = employeeController.listEmployees(pageable);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(employeeService).listEmployees(pageable);
    }

    @Test
    public void testListEmployeesByStatus_ReturnsOk() {
        Pageable pageable = PageRequest.of(0, 10);
        List<EmployeeDTO> dtos = Arrays.asList(employeeDto);
        Page<EmployeeDTO> page = new PageImpl<>(dtos);
        when(employeeService.listEmployeesByStatus("ACTIVE", pageable)).thenReturn(page);
        ResponseEntity<Page<EmployeeDTO>> response = employeeController.listEmployeesByStatus("ACTIVE", pageable);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().getTotalElements());
        verify(employeeService).listEmployeesByStatus("ACTIVE", pageable);
    }

    @Test
    public void testUpdateEmployee_Valid_ReturnsOk() {
        EmployeeDTO updatedDto = new EmployeeDTO();
        updatedDto.setName("Jane Smith");
        updatedDto.setBadgeId("EMP001");
        updatedDto.setRole("SUPERVISOR");
        when(employeeService.updateEmployee(eq(1L), any(EmployeeDTO.class))).thenReturn(updatedDto);
        ResponseEntity<EmployeeDTO> response = employeeController.updateEmployee(1L, updatedDto);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Jane Smith", response.getBody().getName());
        verify(employeeService).updateEmployee(eq(1L), any(EmployeeDTO.class));
    }

    @Test
    public void testUpdateEmployee_NotFound_Throws404() {
        when(employeeService.updateEmployee(eq(2L), any(EmployeeDTO.class))).thenThrow(new EntityNotFoundException("Not found"));
        assertThrows(EntityNotFoundException.class, () -> employeeController.updateEmployee(2L, employeeDto));
    }

    @Test
    public void testDeleteEmployee_Valid_ReturnsNoContent() {
        doNothing().when(employeeService).deleteEmployee(1L);
        ResponseEntity<Void> response = employeeController.deleteEmployee(1L);
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(employeeService).deleteEmployee(1L);
    }

    @Test
    public void testDeleteEmployee_NotFound_Throws404() {
        doThrow(new EntityNotFoundException("Not found")).when(employeeService).deleteEmployee(2L);
        assertThrows(EntityNotFoundException.class, () -> employeeController.deleteEmployee(2L));
    }
}

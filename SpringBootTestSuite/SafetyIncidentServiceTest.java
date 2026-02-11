package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.service.SafetyIncidentService;
import com.example.repository.EmployeeRepository;
import com.example.repository.SafetyIncidentRepository;
import com.example.model.Employee;
import com.example.model.SafetyIncident;
import com.example.exception.EmployeeNotFoundException;
import com.example.exception.IncidentNotFoundException;
import com.example.exception.InvalidIncidentStatusTransitionException;

@ExtendWith(MockitoExtension.class)
public class SafetyIncidentServiceTest {
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private SafetyIncidentRepository incidentRepository;
    @InjectMocks
    private SafetyIncidentService incidentService;

    private Employee employee;
    private SafetyIncident incident;

    @BeforeEach
    void setUp() {
        employee = new Employee();
        employee.setId(1L);
        incident = new SafetyIncident();
        incident.setId(1L);
        incident.setStatus("OPEN");
        incident.setEmployee(employee);
    }

    @Test
    void testCreateIncident_Success() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(incidentRepository.save(any(SafetyIncident.class))).thenReturn(incident);
        SafetyIncident result = incidentService.createIncident(1L, "Fall", "OPEN");
        assertNotNull(result);
        assertEquals("OPEN", result.getStatus());
        verify(incidentRepository).save(any(SafetyIncident.class));
    }

    @Test
    void testCreateIncident_EmployeeNotFound_ThrowsException() {
        when(employeeRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(EmployeeNotFoundException.class, () -> incidentService.createIncident(2L, "Fall", "OPEN"));
    }

    @Test
    void testUpdateIncidentStatus_ToInvestigating_Success() {
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        SafetyIncident result = incidentService.updateIncidentStatus(1L, "INVESTIGATING");
        assertEquals("INVESTIGATING", result.getStatus());
        verify(incidentRepository).save(incident);
    }

    @Test
    void testUpdateIncidentStatus_ToResolved_Success() {
        incident.setStatus("INVESTIGATING");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        SafetyIncident result = incidentService.updateIncidentStatus(1L, "RESOLVED");
        assertEquals("RESOLVED", result.getStatus());
        verify(incidentRepository).save(incident);
    }

    @Test
    void testUpdateIncidentStatus_InvalidTransition_ThrowsException() {
        incident.setStatus("RESOLVED");
        when(incidentRepository.findById(1L)).thenReturn(Optional.of(incident));
        assertThrows(InvalidIncidentStatusTransitionException.class, () -> incidentService.updateIncidentStatus(1L, "OPEN"));
    }

    @Test
    void testGetIncidentsByStatus_ReturnsCorrectList() {
        List<SafetyIncident> incidents = Arrays.asList(incident);
        when(incidentRepository.findByStatus("OPEN")).thenReturn(incidents);
        List<SafetyIncident> result = incidentService.getIncidentsByStatus("OPEN");
        assertEquals(1, result.size());
        assertEquals("OPEN", result.get(0).getStatus());
    }

    @Test
    void testExportOSHA300_Success() {
        List<SafetyIncident> incidents = Arrays.asList(incident);
        when(incidentRepository.findAll()).thenReturn(incidents);
        String csv = incidentService.exportOSHA300();
        assertTrue(csv.contains("OPEN"));
    }

    @Test
    void testGetIncidentMetrics_ReturnsCorrectData() {
        List<SafetyIncident> incidents = Arrays.asList(incident);
        when(incidentRepository.findAll()).thenReturn(incidents);
        Map<String, Integer> metrics = incidentService.getIncidentMetrics();
        assertEquals(1, metrics.get("OPEN"));
    }
}

package com.warehouse.ems.domain.safety;

import com.warehouse.ems.domain.employee.Employee;
import com.warehouse.ems.domain.employee.EmployeeRepository;
import com.warehouse.ems.exception.BusinessException;
import com.warehouse.ems.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("Safety Incident Service Test Suite")
public class SafetyIncidentServiceTest {

    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;

    @Mock
    private InvestigationRepository investigationRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SafetyIncidentServiceImpl safetyIncidentService;

    private Employee testEmployee;
    private SafetyIncident testIncident;
    private Investigation testInvestigation;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        testEmployee.setName("John Doe");

        testIncident = new SafetyIncident();
        testIncident.setId(1L);
        testIncident.setIncidentDate(LocalDateTime.now());
        testIncident.setLocation("Warehouse A - Aisle 5");
        testIncident.setDescription("Forklift collision with pallet rack");
        testIncident.setSeverity(IncidentSeverity.MODERATE);
        testIncident.setStatus(IncidentStatus.OPEN);
        testIncident.setReportedBy(testEmployee);

        testInvestigation = new Investigation();
        testInvestigation.setId(1L);
        testInvestigation.setIncident(testIncident);
        testInvestigation.setInvestigator(testEmployee);
        testInvestigation.setStatus(InvestigationStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("Test create safety incident with valid data")
    public void testCreateSafetyIncidentWithValidData() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        SafetyIncidentDto dto = new SafetyIncidentDto();
        dto.setIncidentDate(LocalDateTime.now());
        dto.setLocation("Warehouse A - Aisle 5");
        dto.setDescription("Forklift collision with pallet rack");
        dto.setSeverity(IncidentSeverity.MODERATE);
        dto.setReportedById(1L);

        // Act
        SafetyIncidentDto result = safetyIncidentService.createIncident(dto);

        // Assert
        assertNotNull(result);
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test create safety incident with null description")
    public void testCreateSafetyIncidentWithNullDescription() {
        // Arrange
        SafetyIncidentDto dto = new SafetyIncidentDto();
        dto.setDescription(null);

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyIncidentService.createIncident(dto);
        });
    }

    @Test
    @DisplayName("Test create safety incident with empty description")
    public void testCreateSafetyIncidentWithEmptyDescription() {
        // Arrange
        SafetyIncidentDto dto = new SafetyIncidentDto();
        dto.setDescription("");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyIncidentService.createIncident(dto);
        });
    }

    @Test
    @DisplayName("Test create safety incident with null location")
    public void testCreateSafetyIncidentWithNullLocation() {
        // Arrange
        SafetyIncidentDto dto = new SafetyIncidentDto();
        dto.setLocation(null);
        dto.setDescription("Test incident");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyIncidentService.createIncident(dto);
        });
    }

    @Test
    @DisplayName("Test create safety incident with null severity")
    public void testCreateSafetyIncidentWithNullSeverity() {
        // Arrange
        SafetyIncidentDto dto = new SafetyIncidentDto();
        dto.setSeverity(null);
        dto.setDescription("Test incident");
        dto.setLocation("Test location");

        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyIncidentService.createIncident(dto);
        });
    }

    @Test
    @DisplayName("Test create safety incident with future date")
    public void testCreateSafetyIncidentWithFutureDate() {
        // Arrange
        SafetyIncidentDto dto = new SafetyIncidentDto();
        dto.setIncidentDate(LocalDateTime.now().plusDays(1));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            safetyIncidentService.createIncident(dto);
        });
    }

    @Test
    @DisplayName("Test create safety incident with minor severity")
    public void testCreateSafetyIncidentWithMinorSeverity() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        SafetyIncidentDto dto = new SafetyIncidentDto();
        dto.setIncidentDate(LocalDateTime.now());
        dto.setLocation("Warehouse A");
        dto.setDescription("Minor slip");
        dto.setSeverity(IncidentSeverity.MINOR);
        dto.setReportedById(1L);

        // Act
        SafetyIncidentDto result = safetyIncidentService.createIncident(dto);

        // Assert
        assertNotNull(result);
        assertEquals(IncidentSeverity.MINOR, result.getSeverity());
    }

    @Test
    @DisplayName("Test create safety incident with critical severity")
    public void testCreateSafetyIncidentWithCriticalSeverity() {
        // Arrange
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        SafetyIncidentDto dto = new SafetyIncidentDto();
        dto.setIncidentDate(LocalDateTime.now());
        dto.setLocation("Warehouse A");
        dto.setDescription("Serious injury");
        dto.setSeverity(IncidentSeverity.CRITICAL);
        dto.setReportedById(1L);

        // Act
        SafetyIncidentDto result = safetyIncidentService.createIncident(dto);

        // Assert
        assertNotNull(result);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test update incident status to investigating")
    public void testUpdateIncidentStatusToInvestigating() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyIncidentService.updateIncidentStatus(1L, IncidentStatus.INVESTIGATING);

        // Assert
        assertNotNull(result);
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Test update incident status to resolved")
    public void testUpdateIncidentStatusToResolved() {
        // Arrange
        testIncident.setStatus(IncidentStatus.INVESTIGATING);
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        SafetyIncidentDto result = safetyIncidentService.updateIncidentStatus(1L, IncidentStatus.RESOLVED);

        // Assert
        assertNotNull(result);
        verify(eventPublisher, times(1)).publishEvent(any());
    }

    @Test
    @DisplayName("Test update non-existent incident status")
    public void testUpdateNonExistentIncidentStatus() {
        // Arrange
        when(safetyIncidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            safetyIncidentService.updateIncidentStatus(999L, IncidentStatus.RESOLVED);
        });
    }

    @Test
    @DisplayName("Test create investigation for incident")
    public void testCreateInvestigationForIncident() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(investigationRepository.save(any(Investigation.class))).thenReturn(testInvestigation);

        InvestigationDto dto = new InvestigationDto();
        dto.setIncidentId(1L);
        dto.setInvestigatorId(1L);
        dto.setFindings("Initial findings");

        // Act
        InvestigationDto result = safetyIncidentService.createInvestigation(dto);

        // Assert
        assertNotNull(result);
        verify(investigationRepository, times(1)).save(any(Investigation.class));
    }

    @Test
    @DisplayName("Test create investigation with null findings")
    public void testCreateInvestigationWithNullFindings() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(investigationRepository.save(any(Investigation.class))).thenReturn(testInvestigation);

        InvestigationDto dto = new InvestigationDto();
        dto.setIncidentId(1L);
        dto.setInvestigatorId(1L);
        dto.setFindings(null);

        // Act
        InvestigationDto result = safetyIncidentService.createInvestigation(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test add corrective action to investigation")
    public void testAddCorrectiveActionToInvestigation() {
        // Arrange
        when(investigationRepository.findById(1L)).thenReturn(Optional.of(testInvestigation));
        when(investigationRepository.save(any(Investigation.class))).thenReturn(testInvestigation);

        // Act
        safetyIncidentService.addCorrectiveAction(1L, "Install additional safety barriers");

        // Assert
        verify(investigationRepository, times(1)).save(any(Investigation.class));
    }

    @Test
    @DisplayName("Test add corrective action with null action")
    public void testAddCorrectiveActionWithNullAction() {
        // Arrange
        when(investigationRepository.findById(1L)).thenReturn(Optional.of(testInvestigation));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            safetyIncidentService.addCorrectiveAction(1L, null);
        });
    }

    @Test
    @DisplayName("Test add corrective action with empty action")
    public void testAddCorrectiveActionWithEmptyAction() {
        // Arrange
        when(investigationRepository.findById(1L)).thenReturn(Optional.of(testInvestigation));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            safetyIncidentService.addCorrectiveAction(1L, "");
        });
    }

    @Test
    @DisplayName("Test generate OSHA report")
    public void testGenerateOSHAReport() {
        // Arrange
        when(safetyIncidentRepository.findIncidentsByDateRange(any(), any()))
            .thenReturn(Arrays.asList(testIncident));

        // Act
        OSHAReportDto result = safetyIncidentService.generateOSHAReport(2024);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getIncidents());
    }

    @Test
    @DisplayName("Test generate OSHA report with invalid year")
    public void testGenerateOSHAReportWithInvalidYear() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            safetyIncidentService.generateOSHAReport(1900);
        });
    }

    @Test
    @DisplayName("Test generate OSHA report with future year")
    public void testGenerateOSHAReportWithFutureYear() {
        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            safetyIncidentService.generateOSHAReport(2100);
        });
    }

    @Test
    @DisplayName("Test get incident by ID - success")
    public void testGetIncidentByIdSuccess() {
        // Arrange
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(testIncident));

        // Act
        SafetyIncidentDto result = safetyIncidentService.getIncidentById(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    @DisplayName("Test get incident by ID - not found")
    public void testGetIncidentByIdNotFound() {
        // Arrange
        when(safetyIncidentRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            safetyIncidentService.getIncidentById(999L);
        });
    }

    @Test
    @DisplayName("Test get incident by null ID")
    public void testGetIncidentByNullId() {
        // Act & Assert
        assertThrows(Exception.class, () -> {
            safetyIncidentService.getIncidentById(null);
        });
    }

    @Test
    @DisplayName("Test create incident with multiple involved employees")
    public void testCreateIncidentWithMultipleInvolvedEmployees() {
        // Arrange
        when(employeeRepository.findById(anyLong())).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        SafetyIncidentDto dto = new SafetyIncidentDto();
        dto.setIncidentDate(LocalDateTime.now());
        dto.setLocation("Warehouse A");
        dto.setDescription("Multi-person incident");
        dto.setSeverity(IncidentSeverity.MODERATE);
        dto.setReportedById(1L);
        dto.setInvolvedEmployeeIds(Arrays.asList(1L, 2L, 3L));

        // Act
        SafetyIncidentDto result = safetyIncidentService.createIncident(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test create incident with maximum length description")
    public void testCreateIncidentWithMaxLengthDescription() {
        // Arrange
        String maxDescription = "A".repeat(2000);
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        SafetyIncidentDto dto = new SafetyIncidentDto();
        dto.setIncidentDate(LocalDateTime.now());
        dto.setLocation("Warehouse A");
        dto.setDescription(maxDescription);
        dto.setSeverity(IncidentSeverity.MODERATE);
        dto.setReportedById(1L);

        // Act
        SafetyIncidentDto result = safetyIncidentService.createIncident(dto);

        // Assert
        assertNotNull(result);
    }

    @Test
    @DisplayName("Test close investigation - success")
    public void testCloseInvestigationSuccess() {
        // Arrange
        when(investigationRepository.findById(1L)).thenReturn(Optional.of(testInvestigation));
        when(investigationRepository.save(any(Investigation.class))).thenReturn(testInvestigation);
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);

        // Act
        safetyIncidentService.closeInvestigation(1L, "Investigation complete");

        // Assert
        verify(investigationRepository, times(1)).save(any(Investigation.class));
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    @DisplayName("Test close investigation with null conclusion")
    public void testCloseInvestigationWithNullConclusion() {
        // Arrange
        when(investigationRepository.findById(1L)).thenReturn(Optional.of(testInvestigation));

        // Act & Assert
        assertThrows(BusinessException.class, () -> {
            safetyIncidentService.closeInvestigation(1L, null);
        });
    }
}
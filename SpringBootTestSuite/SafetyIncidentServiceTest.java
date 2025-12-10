import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.util.*;
import javax.validation.ValidationException;

@ExtendWith(MockitoExtension.class)
public class SafetyIncidentServiceTest {
    @Mock
    private SafetyIncidentRepository safetyIncidentRepository;
    @InjectMocks
    private SafetyIncidentServiceImpl safetyIncidentService;

    private SafetyIncidentDto validIncidentDto;
    private SafetyIncident validIncident;

    @BeforeEach
    void setUp() {
        validIncidentDto = new SafetyIncidentDto();
        validIncidentDto.setDescription("Spill in aisle 3");
        validIncidentDto.setSeverity("High");
        validIncidentDto.setReportedBy(1L);

        validIncident = new SafetyIncident();
        validIncident.setId(1L);
        validIncident.setDescription("Spill in aisle 3");
        validIncident.setSeverity("High");
        validIncident.setReportedBy(1L);
    }

    @Test
    void testReportIncident_ValidInput() {
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);
        SafetyIncident result = safetyIncidentService.report(validIncidentDto);
        assertNotNull(result);
        assertEquals("Spill in aisle 3", result.getDescription());
        verify(safetyIncidentRepository, times(1)).save(any(SafetyIncident.class));
    }

    @Test
    void testReportIncident_NullDescription() {
        validIncidentDto.setDescription(null);
        assertThrows(ValidationException.class, () -> safetyIncidentService.report(validIncidentDto));
    }

    @Test
    void testReportIncident_EmptySeverity() {
        validIncidentDto.setSeverity("");
        assertThrows(ValidationException.class, () -> safetyIncidentService.report(validIncidentDto));
    }

    @Test
    void testUpdateIncident_ValidInput() {
        when(safetyIncidentRepository.findById(1L)).thenReturn(Optional.of(validIncident));
        SafetyIncidentDto updateDto = new SafetyIncidentDto();
        updateDto.setDescription("Updated description");
        updateDto.setSeverity("Low");
        updateDto.setReportedBy(2L);
        validIncident.setDescription("Updated description");
        validIncident.setSeverity("Low");
        validIncident.setReportedBy(2L);
        when(safetyIncidentRepository.save(any(SafetyIncident.class))).thenReturn(validIncident);
        SafetyIncident result = safetyIncidentService.update(1L, updateDto);
        assertEquals("Updated description", result.getDescription());
        assertEquals("Low", result.getSeverity());
        assertEquals(2L, result.getReportedBy());
    }

    @Test
    void testUpdateIncident_NonExistentId() {
        when(safetyIncidentRepository.findById(2L)).thenReturn(Optional.empty());
        SafetyIncidentDto updateDto = new SafetyIncidentDto();
        updateDto.setDescription("desc");
        updateDto.setSeverity("Low");
        updateDto.setReportedBy(2L);
        assertThrows(ResourceNotFoundException.class, () -> safetyIncidentService.update(2L, updateDto));
    }

    @Test
    void testListIncidents_WithResults() {
        List<SafetyIncident> incidents = Arrays.asList(validIncident);
        when(safetyIncidentRepository.findAll()).thenReturn(incidents);
        List<SafetyIncident> result = safetyIncidentService.list();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testListIncidents_EmptyResult() {
        when(safetyIncidentRepository.findAll()).thenReturn(Collections.emptyList());
        List<SafetyIncident> result = safetyIncidentService.list();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}
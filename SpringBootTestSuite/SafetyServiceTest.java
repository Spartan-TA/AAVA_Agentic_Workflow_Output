package SpringBootTestSuite;

import com.example.warehouse.safety.SafetyIncident;
import com.example.warehouse.safety.SafetyService;
import com.example.warehouse.safety.SafetyRepository;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class SafetyServiceTest {
    @Mock
    private SafetyRepository safetyRepository;

    @InjectMocks
    private SafetyService safetyService;

    private AutoCloseable closeable;

    @BeforeEach
    void setUp() {
        closeable = MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    void tearDown() throws Exception {
        closeable.close();
    }

    @Test
    public void reportIncident_ValidInput_ReturnsSafetyIncident() {
        SafetyIncident incident = new SafetyIncident();
        incident.setDescription("Forklift accident");
        incident.setReportedAt(LocalDateTime.now());
        when(safetyRepository.save(any())).thenReturn(incident);
        SafetyIncident result = safetyService.reportIncident(incident);
        assertNotNull(result);
        assertEquals("Forklift accident", result.getDescription());
    }

    @Test
    public void reportIncident_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> safetyService.reportIncident(null));
    }

    @Test
    public void getIncidentById_ValidId_ReturnsSafetyIncident() {
        SafetyIncident incident = new SafetyIncident();
        incident.setId(1L);
        when(safetyRepository.findById(1L)).thenReturn(Optional.of(incident));
        SafetyIncident result = safetyService.getIncidentById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    public void getIncidentById_InvalidId_ThrowsResourceNotFoundException() {
        when(safetyRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> safetyService.getIncidentById(99L));
    }

    @Test
    public void getAllIncidents_ReturnsList() {
        SafetyIncident incident = new SafetyIncident();
        incident.setId(1L);
        when(safetyRepository.findAll()).thenReturn(Collections.singletonList(incident));
        List<SafetyIncident> result = safetyService.getAllIncidents();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getAllIncidents_Empty_ReturnsEmptyList() {
        when(safetyRepository.findAll()).thenReturn(Collections.emptyList());
        List<SafetyIncident> result = safetyService.getAllIncidents();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void reportIncident_EmptyDescription_ThrowsValidationException() {
        SafetyIncident incident = new SafetyIncident();
        incident.setDescription("");
        assertThrows(ValidationException.class, () -> safetyService.reportIncident(incident));
    }
}

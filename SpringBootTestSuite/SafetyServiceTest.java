package com.example.warehouse.test;

import com.example.warehouse.safety.SafetyIncident;
import com.example.warehouse.safety.SafetyRepository;
import com.example.warehouse.safety.SafetyService;
import com.example.warehouse.safety.SafetyController;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SafetyServiceTest {
    @Mock
    private SafetyRepository safetyRepository;
    @InjectMocks
    private SafetyService safetyService;
    private SafetyController safetyController;
    private SafetyIncident testIncident;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        safetyController = new SafetyController(safetyService);
        testIncident = new SafetyIncident(1L, "Slip", "Open", LocalDateTime.now(), "Aisle 3", 1L, "Minor");
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Test
    void testRecordIncident_ValidInput_Success() {
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);
        SafetyIncident created = safetyService.recordIncident(testIncident);
        assertNotNull(created);
        assertEquals("Slip", created.getDescription());
    }

    @Test
    void testRecordIncident_NullInput_ThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> safetyService.recordIncident(null));
    }

    @Test
    void testGetIncidentById_ValidId_ReturnsIncident() {
        when(safetyRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        SafetyIncident found = safetyService.getIncidentById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testGetIncidentById_InvalidId_ThrowsException() {
        when(safetyRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> safetyService.getIncidentById(2L));
    }

    @Test
    void testUpdateIncidentStatus_Valid_Success() {
        when(safetyRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        when(safetyRepository.save(any(SafetyIncident.class))).thenReturn(testIncident);
        SafetyIncident updated = safetyService.updateIncidentStatus(1L, "Investigating");
        assertEquals("Investigating", updated.getStatus());
    }

    @Test
    void testUpdateIncidentStatus_InvalidStatus_ThrowsException() {
        when(safetyRepository.findById(1L)).thenReturn(Optional.of(testIncident));
        assertThrows(IllegalArgumentException.class, () -> safetyService.updateIncidentStatus(1L, "INVALID"));
    }

    @Test
    void testController_RecordIncident_Success() {
        when(safetyService.recordIncident(any(SafetyIncident.class))).thenReturn(testIncident);
        ResponseEntity<SafetyIncident> response = safetyController.recordIncident(testIncident);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Slip", response.getBody().getDescription());
    }

    @Test
    void testController_RecordIncident_NullInput() {
        when(safetyService.recordIncident(any())).thenThrow(new IllegalArgumentException("Null input"));
        assertThrows(IllegalArgumentException.class, () -> safetyController.recordIncident(null));
    }
}

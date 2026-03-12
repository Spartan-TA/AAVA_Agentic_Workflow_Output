package SpringBootTestSuite;

import com.example.warehouse.integration.IntegrationService;
import com.example.warehouse.integration.IntegrationRepository;
import com.example.warehouse.integration.HRISPayload;
import com.example.warehouse.integration.WMSPayload;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class IntegrationServiceTest {
    @Mock
    private IntegrationRepository integrationRepository;

    @InjectMocks
    private IntegrationService integrationService;

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
    public void syncHRIS_ValidPayload_Success() {
        HRISPayload payload = new HRISPayload();
        payload.setEmployeeId(1L);
        doNothing().when(integrationRepository).syncHRIS(any());
        assertDoesNotThrow(() -> integrationService.syncHRIS(payload));
    }

    @Test
    public void syncHRIS_NullPayload_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> integrationService.syncHRIS(null));
    }

    @Test
    public void syncWMS_ValidPayload_Success() {
        WMSPayload payload = new WMSPayload();
        payload.setDepartmentId(1L);
        doNothing().when(integrationRepository).syncWMS(any());
        assertDoesNotThrow(() -> integrationService.syncWMS(payload));
    }

    @Test
    public void syncWMS_NullPayload_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> integrationService.syncWMS(null));
    }

    @Test
    public void getHRISPayloadById_ValidId_ReturnsPayload() {
        HRISPayload payload = new HRISPayload();
        payload.setEmployeeId(1L);
        when(integrationRepository.findHRISPayloadById(1L)).thenReturn(Optional.of(payload));
        HRISPayload result = integrationService.getHRISPayloadById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    public void getHRISPayloadById_InvalidId_ThrowsResourceNotFoundException() {
        when(integrationRepository.findHRISPayloadById(99L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> integrationService.getHRISPayloadById(99L));
    }
}

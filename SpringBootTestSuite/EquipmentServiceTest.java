package SpringBootTestSuite;

import com.example.warehouse.equipment.Asset;
import com.example.warehouse.equipment.AssetAssignment;
import com.example.warehouse.equipment.EquipmentService;
import com.example.warehouse.equipment.EquipmentRepository;
import com.example.warehouse.exception.ResourceNotFoundException;
import com.example.warehouse.exception.ValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EquipmentServiceTest {
    @Mock
    private EquipmentRepository equipmentRepository;

    @InjectMocks
    private EquipmentService equipmentService;

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
    public void createAsset_ValidInput_ReturnsAsset() {
        Asset asset = new Asset();
        asset.setName("Forklift");
        when(equipmentRepository.saveAsset(any())).thenReturn(asset);
        Asset result = equipmentService.createAsset(asset);
        assertNotNull(result);
        assertEquals("Forklift", result.getName());
    }

    @Test
    public void createAsset_NullInput_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> equipmentService.createAsset(null));
    }

    @Test
    public void assignAsset_ValidInput_ReturnsAssetAssignment() {
        AssetAssignment assignment = new AssetAssignment();
        assignment.setEmployeeId(1L);
        assignment.setAssetId(1L);
        assignment.setAssignedDate(LocalDate.now());
        when(equipmentRepository.saveAssetAssignment(any())).thenReturn(assignment);
        AssetAssignment result = equipmentService.assignAsset(1L, 1L, LocalDate.now());
        assertNotNull(result);
        assertEquals(1L, result.getEmployeeId());
    }

    @Test
    public void assignAsset_NullEmployeeId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> equipmentService.assignAsset(null, 1L, LocalDate.now()));
    }

    @Test
    public void assignAsset_NullAssetId_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> equipmentService.assignAsset(1L, null, LocalDate.now()));
    }

    @Test
    public void assignAsset_NullAssignedDate_ThrowsValidationException() {
        assertThrows(ValidationException.class, () -> equipmentService.assignAsset(1L, 1L, null));
    }

    @Test
    public void getAssets_ReturnsList() {
        Asset asset = new Asset();
        asset.setId(1L);
        when(equipmentRepository.findAllAssets()).thenReturn(Collections.singletonList(asset));
        List<Asset> result = equipmentService.getAssets();
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    public void getAssets_Empty_ReturnsEmptyList() {
        when(equipmentRepository.findAllAssets()).thenReturn(Collections.emptyList());
        List<Asset> result = equipmentService.getAssets();
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }
}

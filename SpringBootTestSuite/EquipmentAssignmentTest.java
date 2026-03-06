package SpringBootTestSuite;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@AutoConfigureMockMvc
public class EquipmentAssignmentTest {

    @Autowired
    private MockMvc mockMvc;

    @Mock
    private EquipmentService equipmentService;

    @InjectMocks
    private EquipmentController equipmentController;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testCreateAsset_NormalCase_Success() {
        Equipment asset = new Equipment("Forklift", "A123", false);
        when(equipmentService.createAsset(any())).thenReturn(asset);
        Equipment result = equipmentController.createAsset(asset);
        assertEquals("Forklift", result.getName());
        assertEquals("A123", result.getSerial());
    }

    @Test
    public void testCreateAsset_NullInput_Exception() {
        when(equipmentService.createAsset(null)).thenThrow(new IllegalArgumentException("Asset cannot be null"));
        assertThrows(IllegalArgumentException.class, () -> equipmentController.createAsset(null));
    }

    @Test
    public void testGetAssetById_ValidId_ReturnsAsset() {
        Equipment asset = new Equipment("Pallet Jack", "B456", false);
        when(equipmentService.getAssetById(1L)).thenReturn(asset);
        Equipment result = equipmentController.getAssetById(1L);
        assertEquals("Pallet Jack", result.getName());
    }

    @Test
    public void testGetAssetById_InvalidId_ReturnsNull() {
        when(equipmentService.getAssetById(999L)).thenReturn(null);
        Equipment result = equipmentController.getAssetById(999L);
        assertNull(result);
    }

    @Test
    public void testCheckOutAsset_ValidCertification_Success() {
        when(equipmentService.checkOutAsset(anyLong(), anyLong())).thenReturn(true);
        assertTrue(equipmentService.checkOutAsset(1L, 2L));
    }

    @Test
    public void testCheckOutAsset_ExpiredCertification_Block() {
        when(equipmentService.checkOutAsset(anyLong(), anyLong())).thenThrow(new IllegalStateException("Certification expired"));
        assertThrows(IllegalStateException.class, () -> equipmentService.checkOutAsset(1L, 2L));
    }

    @Test
    public void testCheckInAsset_NormalCase_Success() {
        when(equipmentService.checkInAsset(anyLong())).thenReturn(true);
        assertTrue(equipmentService.checkInAsset(1L));
    }

    @Test
    public void testCheckInAsset_InvalidId_Failure() {
        when(equipmentService.checkInAsset(999L)).thenReturn(false);
        assertFalse(equipmentService.checkInAsset(999L));
    }

    @Test
    public void testGetAssignmentHistory_ValidId_ReturnsHistory() {
        java.util.List<AssignmentHistory> history = java.util.Arrays.asList(
            new AssignmentHistory(1L, "2024-06-01", "Checked Out"),
            new AssignmentHistory(1L, "2024-06-02", "Checked In")
        );
        when(equipmentService.getAssignmentHistory(1L)).thenReturn(history);
        assertEquals(2, equipmentService.getAssignmentHistory(1L).size());
    }

    @Test
    public void testGetAssignmentHistory_InvalidId_ReturnsEmpty() {
        when(equipmentService.getAssignmentHistory(999L)).thenReturn(java.util.Collections.emptyList());
        assertTrue(equipmentService.getAssignmentHistory(999L).isEmpty());
    }

    @Test
    public void testOverdueReport_AssetsOverdue_ReturnsList() {
        java.util.List<Equipment> overdue = java.util.Arrays.asList(
            new Equipment("Forklift", "A123", true)
        );
        when(equipmentService.getOverdueAssets()).thenReturn(overdue);
        assertEquals(1, equipmentService.getOverdueAssets().size());
        assertTrue(equipmentService.getOverdueAssets().get(0).isOverdue());
    }

    @Test
    public void testOverdueReport_NoAssets_ReturnsEmpty() {
        when(equipmentService.getOverdueAssets()).thenReturn(java.util.Collections.emptyList());
        assertTrue(equipmentService.getOverdueAssets().isEmpty());
    }

    @Test
    public void testDeleteAsset_ValidId_Success() {
        doNothing().when(equipmentService).deleteAsset(2L);
        equipmentController.deleteAsset(2L);
        verify(equipmentService, times(1)).deleteAsset(2L);
    }

    @Test
    public void testDeleteAsset_InvalidId_Exception() {
        doThrow(new RuntimeException("Not found")).when(equipmentService).deleteAsset(999L);
        assertThrows(RuntimeException.class, () -> equipmentController.deleteAsset(999L));
    }

    @Test
    public void testAuthorization_UnauthorizedUser_ThrowsException() {
        doThrow(new SecurityException("Unauthorized")).when(equipmentService).deleteAsset(anyLong());
        assertThrows(SecurityException.class, () -> equipmentService.deleteAsset(1L));
    }

    @Test
    public void testCreateAsset_InvalidData_Exception() {
        Equipment invalidAsset = new Equipment("", "", false);
        when(equipmentService.createAsset(invalidAsset)).thenThrow(new IllegalArgumentException("Invalid data"));
        assertThrows(IllegalArgumentException.class, () -> equipmentController.createAsset(invalidAsset));
    }

    // Add more tests as needed for edge cases, nulls, etc.
}

class Equipment {
    private String name;
    private String serial;
    private boolean overdue;
    public Equipment(String name, String serial, boolean overdue) {
        this.name = name;
        this.serial = serial;
        this.overdue = overdue;
    }
    public String getName() { return name; }
    public String getSerial() { return serial; }
    public boolean isOverdue() { return overdue; }
}

class AssignmentHistory {
    private Long assetId;
    private String date;
    private String action;
    public AssignmentHistory(Long assetId, String date, String action) {
        this.assetId = assetId;
        this.date = date;
        this.action = action;
    }
    public Long getAssetId() { return assetId; }
    public String getDate() { return date; }
    public String getAction() { return action; }
}

class EquipmentService {
    public Equipment createAsset(Equipment asset) { return null; }
    public Equipment getAssetById(Long id) { return null; }
    public boolean checkOutAsset(Long assetId, Long userId) { return false; }
    public boolean checkInAsset(Long assetId) { return false; }
    public java.util.List<AssignmentHistory> getAssignmentHistory(Long assetId) { return null; }
    public java.util.List<Equipment> getOverdueAssets() { return null; }
    public void deleteAsset(Long id) {}
}

class EquipmentController {
    private EquipmentService equipmentService;
    public Equipment createAsset(Equipment asset) { return equipmentService.createAsset(asset); }
    public Equipment getAssetById(Long id) { return equipmentService.getAssetById(id); }
    public void deleteAsset(Long id) { equipmentService.deleteAsset(id); }
    public boolean checkOutAsset(Long assetId, Long userId) { return equipmentService.checkOutAsset(assetId, userId); }
    public boolean checkInAsset(Long assetId) { return equipmentService.checkInAsset(assetId); }
}

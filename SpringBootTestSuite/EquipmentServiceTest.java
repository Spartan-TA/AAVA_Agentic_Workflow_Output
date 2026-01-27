package SpringBootTestSuite;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.*;

public class EquipmentServiceTest {
    @Mock
    private EquipmentRepository equipmentRepository;
    @Mock
    private EmployeeRepository employeeRepository;
    @Mock
    private TrainingRepository trainingRepository;

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
    void testAssignAssetToEmployee_Valid_Success() {
        Equipment asset = new Equipment(1L, "Forklift", "AVAILABLE", null);
        Employee employee = new Employee("John Doe", "B123", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        Certification cert = new Certification(1L, 1L, "Forklift", new Date(), new Date(System.currentTimeMillis()+31536000000L), "ACTIVE");
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee));
        when(trainingRepository.findByEmployeeIdAndType(1L, "Forklift")).thenReturn(Optional.of(cert));
        equipmentService.assignAssetToEmployee(1L, 1L);
        verify(equipmentRepository).save(asset);
    }

    @Test
    void testAssignAssetToEmployee_MissingCertification_ThrowsException() {
        Equipment asset = new Equipment(1L, "Forklift", "AVAILABLE", null);
        Employee employee = new Employee("Jane Doe", "B124", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        when(trainingRepository.findByEmployeeIdAndType(2L, "Forklift")).thenReturn(Optional.empty());
        assertThrows(MissingCertificationException.class, () -> equipmentService.assignAssetToEmployee(1L, 2L));
    }

    @Test
    void testCheckInAsset_Valid_Success() {
        Equipment asset = new Equipment(1L, "Forklift", "ASSIGNED", 1L);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(asset));
        equipmentService.checkInAsset(1L);
        verify(equipmentRepository).save(asset);
        assertNull(asset.getAssignedEmployeeId());
        assertEquals("AVAILABLE", asset.getStatus());
    }

    @Test
    void testCheckOutAsset_AlreadyAssigned_ThrowsException() {
        Equipment asset = new Equipment(1L, "Forklift", "ASSIGNED", 1L);
        when(equipmentRepository.findById(1L)).thenReturn(Optional.of(asset));
        assertThrows(AssetAlreadyAssignedException.class, () -> equipmentService.checkOutAsset(1L, 2L));
    }

    @Test
    void testCheckOutAsset_Valid_Success() {
        Equipment asset = new Equipment(2L, "Scanner", "AVAILABLE", null);
        Employee employee = new Employee("Jane Doe", "B124", "WORKER", "Shipping", "A", new Date(), "ACTIVE");
        when(equipmentRepository.findById(2L)).thenReturn(Optional.of(asset));
        when(employeeRepository.findById(2L)).thenReturn(Optional.of(employee));
        equipmentService.checkOutAsset(2L, 2L);
        verify(equipmentRepository).save(asset);
        assertEquals(2L, asset.getAssignedEmployeeId());
        assertEquals("ASSIGNED", asset.getStatus());
    }

    @Test
    void testGetAssetHistory_Valid_Success() {
        List<AssetHistory> history = Arrays.asList(
            new AssetHistory(1L, 1L, "ASSIGNED", new Date()),
            new AssetHistory(1L, 2L, "CHECKED_IN", new Date())
        );
        when(equipmentRepository.getAssetHistory(1L)).thenReturn(history);
        List<AssetHistory> result = equipmentService.getAssetHistory(1L);
        assertEquals(2, result.size());
    }

    // Integration scenario: Overdue asset return report
    @Test
    void testOverdueAssetReturnReport_Success() {
        List<Equipment> overdueAssets = Arrays.asList(
            new Equipment(3L, "PPE", "ASSIGNED", 1L)
        );
        when(equipmentRepository.findOverdueAssets()).thenReturn(overdueAssets);
        List<Equipment> result = equipmentService.getOverdueAssetsReport();
        assertEquals(1, result.size());
    }
}

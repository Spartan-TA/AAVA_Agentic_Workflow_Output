package com.warehouse.management.asset;

import com.warehouse.management.asset.AssetService;
import com.warehouse.management.asset.Asset;
import com.warehouse.management.employee.Employee;
import org.junit.jupiter.api.*;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;

    @InjectMocks
    private AssetService assetService;

    private Employee employee;
    private Asset asset;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        employee = new Employee(1L, "John Doe", "BADGE123", "WORKER", "Logistics", "A", new Date(), "ACTIVE");
        asset = new Asset(1L, "Forklift", "Operational", employee, new Date());
    }

    @Test
    void testAssignAsset_Valid() {
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        Asset result = assetService.assignAsset(employee, "Forklift");
        assertNotNull(result);
        assertEquals("Forklift", result.getType());
    }

    @Test
    void testCheckInAsset_Valid() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        Asset result = assetService.checkIn(1L);
        assertEquals("Operational", result.getStatus());
    }

    @Test
    void testCheckOutAsset_Valid() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        Asset result = assetService.checkOut(1L, employee);
        assertEquals(employee.getId(), result.getAssignedTo().getId());
    }

    @Test
    void testValidateCertification_Expired() {
        when(assetService.validateCertification(employee, "Forklift")).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> assetService.assignAsset(employee, "Forklift"));
    }
}
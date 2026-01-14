package com.example.warehouse.test;

import com.example.warehouse.asset.Asset;
import com.example.warehouse.asset.AssetRepository;
import com.example.warehouse.asset.AssetService;
import com.example.warehouse.asset.AssetController;
import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetServiceTest {
    @Mock
    private AssetRepository assetRepository;
    @InjectMocks
    private AssetService assetService;
    private AssetController assetController;
    private Asset testAsset;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        assetController = new AssetController(assetService);
        testAsset = new Asset(1L, "Forklift", "A123", "AVAILABLE", 1L, "OK");
    }

    @AfterEach
    void tearDown() {
        // Cleanup if needed
    }

    @Test
    void testRegisterAsset_ValidInput_Success() {
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);
        Asset created = assetService.registerAsset(testAsset);
        assertNotNull(created);
        assertEquals("Forklift", created.getName());
    }

    @Test
    void testRegisterAsset_DuplicateSerial_ThrowsException() {
        when(assetRepository.findBySerialNumber("A123")).thenReturn(Optional.of(testAsset));
        assertThrows(IllegalArgumentException.class, () -> assetService.registerAsset(testAsset));
    }

    @Test
    void testCheckOutAsset_Valid_Success() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(assetRepository.save(any(Asset.class))).thenReturn(testAsset);
        Asset checkedOut = assetService.checkOutAsset(1L, 1L);
        assertEquals("ASSIGNED", checkedOut.getStatus());
    }

    @Test
    void testCheckOutAsset_InvalidCert_ThrowsException() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        when(assetService.hasValidCertification(anyLong(), anyString())).thenReturn(false);
        assertThrows(IllegalStateException.class, () -> assetService.checkOutAsset(1L, 1L));
    }

    @Test
    void testGetAssetById_ValidId_ReturnsAsset() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(testAsset));
        Asset found = assetService.getAssetById(1L);
        assertNotNull(found);
        assertEquals(1L, found.getId());
    }

    @Test
    void testGetAssetById_InvalidId_ThrowsException() {
        when(assetRepository.findById(2L)).thenReturn(Optional.empty());
        assertThrows(NoSuchElementException.class, () -> assetService.getAssetById(2L));
    }

    @Test
    void testController_RegisterAsset_Success() {
        when(assetService.registerAsset(any(Asset.class))).thenReturn(testAsset);
        ResponseEntity<Asset> response = assetController.registerAsset(testAsset);
        assertEquals(201, response.getStatusCodeValue());
        assertEquals("Forklift", response.getBody().getName());
    }

    @Test
    void testController_RegisterAsset_Duplicate() {
        when(assetService.registerAsset(any(Asset.class))).thenThrow(new IllegalArgumentException("Duplicate"));
        assertThrows(IllegalArgumentException.class, () -> assetController.registerAsset(testAsset));
    }
}

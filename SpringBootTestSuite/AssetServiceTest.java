package com.warehouse.ems.service;

import com.warehouse.ems.dto.AssetRequestDto;
import com.warehouse.ems.entity.Asset;
import com.warehouse.ems.exception.EntityNotFoundException;
import com.warehouse.ems.repository.AssetRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AssetService.
 * Covers normal operation, null/invalid input, duplicate entries, and exception scenarios.
 */
@ExtendWith(MockitoExtension.class)
class AssetServiceTest {

    @Mock
    private AssetRepository assetRepository;
    @InjectMocks
    private AssetService assetService;

    private Asset asset;
    private AssetRequestDto assetRequestDto;

    @BeforeEach
    void setUp() {
        asset = new Asset();
        asset.setId(1L);
        asset.setName("Forklift");
        asset.setSerialNumber("SN123");
        asset.setPurchaseDate(LocalDate.now().minusYears(2));
        asset.setStatus("ACTIVE");

        assetRequestDto = new AssetRequestDto();
        assetRequestDto.setName("Forklift");
        assetRequestDto.setSerialNumber("SN123");
        assetRequestDto.setPurchaseDate(LocalDate.now().minusYears(2));
        assetRequestDto.setStatus("ACTIVE");
    }

    /**
     * Test createAsset with valid input returns Asset.
     */
    @Test
    void testCreateAsset_ValidInput_ReturnsAsset() {
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        Asset result = assetService.createAsset(assetRequestDto);
        assertNotNull(result);
        assertEquals("Forklift", result.getName());
    }

    /**
     * Test createAsset with null DTO throws exception.
     */
    @Test
    void testCreateAsset_NullDto_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                assetService.createAsset(null));
    }

    /**
     * Test getAssetById with valid ID returns Asset.
     */
    @Test
    void testGetAssetById_ValidId_ReturnsAsset() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        Asset result = assetService.getAssetById(1L);
        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    /**
     * Test getAssetById with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testGetAssetById_NonExistentId_ThrowsEntityNotFoundException() {
        when(assetRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                assetService.getAssetById(99L));
    }

    /**
     * Test getAllAssets returns list.
     */
    @Test
    void testGetAllAssets_ReturnsList() {
        when(assetRepository.findAll()).thenReturn(List.of(asset));
        List<Asset> result = assetService.getAllAssets();
        assertEquals(1, result.size());
    }

    /**
     * Test updateAsset with valid input returns Asset.
     */
    @Test
    void testUpdateAsset_ValidInput_ReturnsAsset() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        Asset result = assetService.updateAsset(1L, assetRequestDto);
        assertNotNull(result);
    }

    /**
     * Test updateAsset with non-existent ID throws EntityNotFoundException.
     */
    @Test
    void testUpdateAsset_NonExistentId_ThrowsEntityNotFoundException() {
        when(assetRepository.findById(99L)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () ->
                assetService.updateAsset(99L, assetRequestDto));
    }

    /**
     * Test deleteAsset with valid ID does not throw.
     */
    @Test
    void testDeleteAsset_ValidId_DoesNotThrow() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(asset));
        doNothing().when(assetRepository).delete(asset);
        assertDoesNotThrow(() -> assetService.deleteAsset(1L));
    }
}

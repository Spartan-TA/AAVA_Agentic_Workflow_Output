package com.warehouse.equipment.service;

import com.warehouse.equipment.entity.Asset;
import com.warehouse.equipment.entity.AssetAssignment;
import com.warehouse.equipment.repository.AssetRepository;
import com.warehouse.equipment.repository.AssetAssignmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AssetServiceTest {
    @Mock
    private AssetRepository assetRepository;
    @Mock
    private AssetAssignmentRepository assetAssignmentRepository;

    @InjectMocks
    private AssetService assetService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testAssignAsset() {
        Asset asset = Asset.builder().id(1L).type("Forklift").status(Asset.Status.AVAILABLE).build();
        when(assetRepository.findById(1L)).thenReturn(java.util.Optional.of(asset));
        when(assetRepository.save(any(Asset.class))).thenReturn(asset);
        AssetAssignment assignment = AssetAssignment.builder().assetId(1L).employeeId(2L).assignedDate(LocalDate.now()).build();
        when(assetAssignmentRepository.save(any(AssetAssignment.class))).thenReturn(assignment);
        AssetAssignment result = assetService.assignAsset(1L, 2L);
        assertEquals(1L, result.getAssetId());
        assertEquals(2L, result.getEmployeeId());
    }
}

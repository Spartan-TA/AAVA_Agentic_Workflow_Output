package com.company.warehouse.asset.service;

import com.company.warehouse.asset.domain.*;
import com.company.warehouse.asset.dto.*;
import com.company.warehouse.asset.repository.*;
import com.company.warehouse.employee.domain.Employee;
import com.company.warehouse.employee.repository.EmployeeRepository;
import com.company.warehouse.certification.service.CertificationService;
import com.company.warehouse.common.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.time.LocalDateTime;
import java.util.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@DisplayName("Asset Service Tests")
public class AssetServiceTest {
    @Mock private AssetRepository assetRepository;
    @Mock private AssetAssignmentRepository assetAssignmentRepository;
    @Mock private EmployeeRepository employeeRepository;
    @Mock private CertificationService certificationService;
    @InjectMocks private AssetService assetService;
    private Asset forklift;
    private Employee testEmployee;
    private AssetAssignment assignment;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        forklift = new Asset();
        forklift.setId(1L);
        forklift.setAssetTag("FORK001");
        forklift.setAssetType(AssetType.FORKLIFT);
        forklift.setCondition(AssetCondition.GOOD);
        forklift.setRequiredCertificationId(1L);
        testEmployee = new Employee();
        testEmployee.setId(1L);
        testEmployee.setBadgeId("EMP001");
        assignment = new AssetAssignment();
        assignment.setId(1L);
        assignment.setAsset(forklift);
        assignment.setEmployee(testEmployee);
        assignment.setCheckoutDate(LocalDateTime.now());
    }

    @Test
    @DisplayName("Test registerAsset with valid data")
    public void testRegisterAsset_ValidData() {
        when(assetRepository.save(any(Asset.class))).thenReturn(forklift);
        AssetDTO result = assetService.registerAsset(new AssetCreateDTO());
        assertNotNull(result);
        verify(assetRepository, times(1)).save(any(Asset.class));
    }

    @Test
    @DisplayName("Test assignAsset with valid certification")
    public void testAssignAsset_ValidCertification() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(forklift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationService.validateCertification(1L, 1L)).thenReturn(true);
        when(assetAssignmentRepository.save(any(AssetAssignment.class))).thenReturn(assignment);
        AssetAssignmentDTO result = assetService.assignAsset(1L, 1L);
        assertNotNull(result);
        verify(assetAssignmentRepository, times(1)).save(any(AssetAssignment.class));
    }

    @Test
    @DisplayName("Test assignAsset without required certification")
    public void testAssignAsset_MissingCertification() {
        when(assetRepository.findById(1L)).thenReturn(Optional.of(forklift));
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(testEmployee));
        when(certificationService.validateCertification(1L, 1L)).thenReturn(false);
        assertThrows(BusinessException.class, () -> assetService.assignAsset(1L, 1L));
        verify(assetAssignmentRepository, never()).save(any(AssetAssignment.class));
    }

    @Test
    @DisplayName("Test returnAsset with valid assignment")
    public void testReturnAsset_ValidAssignment() {
        when(assetAssignmentRepository.findById(1L)).thenReturn(Optional.of(assignment));
        when(assetAssignmentRepository.save(any(AssetAssignment.class))).thenReturn(assignment);
        AssetAssignmentDTO result = assetService.returnAsset(1L);
        assertNotNull(result);
        assertNotNull(assignment.getReturnDate());
    }

    @Test
    @DisplayName("Test getAssetHistory for asset")
    public void testGetAssetHistory_ValidAsset() {
        when(assetAssignmentRepository.findByAssetId(1L)).thenReturn(Arrays.asList(assignment));
        List<AssetAssignmentDTO> results = assetService.getAssetHistory(1L);
        assertNotNull(results);
        assertFalse(results.isEmpty());
    }

    @Test
    @DisplayName("Test getOverdueAssets")
    public void testGetOverdueAssets() {
        when(assetAssignmentRepository.findOverdueAssignments(any(LocalDateTime.class))).thenReturn(Arrays.asList(assignment));
        List<AssetAssignmentDTO> results = assetService.getOverdueAssets();
        assertNotNull(results);
    }
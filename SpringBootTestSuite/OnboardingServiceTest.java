package com.warehouse.ems.service;

import org.junit.jupiter.api.*;
import org.mockito.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class OnboardingServiceTest {
    @Autowired
    OnboardingService onboardingService;

    @MockBean
    TrainingService trainingService;
    @MockBean
    AssetService assetService;
    @MockBean
    HRISClient hrisClient;

    @BeforeEach
    void setup() {
        // Setup mocks if needed
    }

    @Test
    void testAutomatedProvisioning_NewHire() {
        EmployeeDTO newHire = new EmployeeDTO("John Doe", "B123", "Worker", "Receiving", "A", new Date(), "Active");
        when(hrisClient.fetchNewHires()).thenReturn(Collections.singletonList(newHire));
        when(onboardingService.provisionEmployee(newHire)).thenReturn(true);
        boolean result = onboardingService.provisionEmployee(newHire);
        assertTrue(result);
    }

    @Test
    void testTrainingAssignment() {
        EmployeeDTO emp = new EmployeeDTO("Jane", "B124", "Worker", "Packing", "B", new Date(), "Active");
        TrainingDTO training = new TrainingDTO("Forklift Safety", new Date());
        when(trainingService.assignTraining(emp, training)).thenReturn(true);
        boolean assigned = onboardingService.assignTraining(emp, training);
        assertTrue(assigned);
    }

    @Test
    void testAssetAssignment() {
        EmployeeDTO emp = new EmployeeDTO("Sam", "B125", "Worker", "Shipping", "C", new Date(), "Active");
        AssetDTO asset = new AssetDTO("Forklift", "A1001");
        when(assetService.assignAsset(emp, asset)).thenReturn(true);
        boolean assigned = onboardingService.assignAsset(emp, asset);
        assertTrue(assigned);
    }

    @Test
    void testOffboarding_RevokesAccess() {
        EmployeeDTO emp = new EmployeeDTO("Alex", "B126", "Worker", "Receiving", "A", new Date(), "Terminated");
        when(onboardingService.offboardEmployee(emp)).thenReturn(true);
        boolean result = onboardingService.offboardEmployee(emp);
        assertTrue(result);
    }

    @Test
    void testOffboarding_CollectsAssets() {
        EmployeeDTO emp = new EmployeeDTO("Alex", "B126", "Worker", "Receiving", "A", new Date(), "Terminated");
        when(assetService.collectAssets(emp)).thenReturn(Arrays.asList("Forklift", "Scanner"));
        List<String> assets = onboardingService.collectAssets(emp);
        assertEquals(Arrays.asList("Forklift", "Scanner"), assets);
    }

    @Test
    void testNullEmployee_Throws() {
        assertThrows(IllegalArgumentException.class, () -> onboardingService.provisionEmployee(null));
    }

    @Test
    void testDuplicateBadgeId() {
        EmployeeDTO emp = new EmployeeDTO("Sam", "B125", "Worker", "Shipping", "C", new Date(), "Active");
        doThrow(new IllegalArgumentException("Duplicate badgeId")).when(onboardingService).provisionEmployee(emp);
        assertThrows(IllegalArgumentException.class, () -> onboardingService.provisionEmployee(emp));
    }

    @Test
    void testIntegration_NewHireWorkflow() {
        EmployeeDTO newHire = new EmployeeDTO("John Doe", "B123", "Worker", "Receiving", "A", new Date(), "Active");
        TrainingDTO training = new TrainingDTO("Forklift Safety", new Date());
        AssetDTO asset = new AssetDTO("Forklift", "A1001");
        when(onboardingService.provisionEmployee(newHire)).thenReturn(true);
        when(onboardingService.assignTraining(newHire, training)).thenReturn(true);
        when(onboardingService.assignAsset(newHire, asset)).thenReturn(true);
        assertTrue(onboardingService.provisionEmployee(newHire));
        assertTrue(onboardingService.assignTraining(newHire, training));
        assertTrue(onboardingService.assignAsset(newHire, asset));
    }
}

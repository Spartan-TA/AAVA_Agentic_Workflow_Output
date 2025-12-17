package com.warehouse.management.controller;

import com.warehouse.management.service.CertificationService;
import com.warehouse.management.dto.CertificationDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive controller tests for CertificationController
 * Tests REST endpoints, status codes, security, and validation
 */
@WebMvcTest(CertificationController.class)
public class CertificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CertificationService certificationService;

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test GET /api/certifications returns 200 OK")
    void testGetAllCertifications_Returns200() throws Exception {
        mockMvc.perform(get("/api/certifications"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test POST /api/certifications returns 201 Created")
    void testCreateCertification_Returns201() throws Exception {
        String certJson = "{"name":"Forklift","expiryDate":"2025-01-01"}";
        mockMvc.perform(post("/api/certifications")
            .contentType(MediaType.APPLICATION_JSON)
            .content(certJson))
            .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test POST /api/certifications with WORKER role returns 403 Forbidden")
    void testCreateCertification_WorkerRole_Returns403() throws Exception {
        String certJson = "{"name":"Forklift","expiryDate":"2025-01-01"}";
        mockMvc.perform(post("/api/certifications")
            .contentType(MediaType.APPLICATION_JSON)
            .content(certJson))
            .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Test GET /api/certifications without authentication returns 401 Unauthorized")
    void testGetAllCertifications_NoAuth_Returns401() throws Exception {
        mockMvc.perform(get("/api/certifications"))
            .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test PUT /api/certifications/{id} returns 200 OK")
    void testUpdateCertification_Returns200() throws Exception {
        String certJson = "{"name":"Hazmat","expiryDate":"2026-01-01"}";
        mockMvc.perform(put("/api/certifications/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(certJson))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test POST /api/certifications/assign returns 200 OK")
    void testAssignCertification_Returns200() throws Exception {
        String assignJson = "{"certificationId":1,"employeeId":100}";
        mockMvc.perform(post("/api/certifications/assign")
            .contentType(MediaType.APPLICATION_JSON)
            .content(assignJson))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "WORKER")
    @DisplayName("Test GET /api/certifications/employee/{id} returns 200 OK")
    void testGetCertificationsByEmployee_Returns200() throws Exception {
        mockMvc.perform(get("/api/certifications/employee/100"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test POST /api/certifications with invalid data returns 400 Bad Request")
    void testCreateCertification_InvalidData_Returns400() throws Exception {
        String invalidJson = "{"name":""}";
        mockMvc.perform(post("/api/certifications")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidJson))
            .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test GET /api/certifications/{id} with invalid ID returns 404 Not Found")
    void testGetCertificationById_InvalidId_Returns404() throws Exception {
        when(certificationService.getCertificationById(999L)).thenThrow(new CertificationNotFoundException());
        mockMvc.perform(get("/api/certifications/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "SUPERVISOR")
    @DisplayName("Test DELETE /api/certifications/{id} returns 204 No Content")
    void testDeleteCertification_Returns204() throws Exception {
        mockMvc.perform(delete("/api/certifications/1"))
            .andExpect(status().isNoContent());
    }
}

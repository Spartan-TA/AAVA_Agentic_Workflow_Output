package com.wms.certification.controllers;

import com.wms.certification.dtos.CertificationDto;
import com.wms.certification.dtos.EmployeeCertificationDto;
import com.wms.certification.services.CertificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for certification management
 */
@RestController
@RequestMapping("/api/certification")
@RequiredArgsConstructor
public class CertificationController {
    private final CertificationService certificationService;

    /**
     * Create a new certification type
     */
    @PostMapping("/types")
    public ResponseEntity<CertificationDto> createCertification(@RequestBody CertificationDto dto) {
        return ResponseEntity.ok(certificationService.createCertification(dto));
    }

    /**
     * Get all certification types
     */
    @GetMapping("/types")
    public ResponseEntity<List<CertificationDto>> getAllCertifications() {
        return ResponseEntity.ok(certificationService.getAllCertifications());
    }

    /**
     * Assign a certification to an employee
     */
    @PostMapping("/assignments")
    public ResponseEntity<EmployeeCertificationDto> assignCertification(@RequestBody EmployeeCertificationDto dto) {
        return ResponseEntity.ok(certificationService.assignCertification(dto));
    }

    /**
     * Get all certifications for an employee
     */
    @GetMapping("/assignments/employee/{employeeId}")
    public ResponseEntity<List<EmployeeCertificationDto>> getCertificationsForEmployee(@PathVariable Long employeeId) {
        return ResponseEntity.ok(certificationService.getCertificationsForEmployee(employeeId));
    }
}

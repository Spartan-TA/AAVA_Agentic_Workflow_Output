package com.company.project.controller;

import com.company.project.dto.CertificationDto;
import com.company.project.service.CertificationService;
import com.company.project.mapper.CertificationMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/certifications")
@Tag(name = "Certification Management", description = "Manage employee certifications")
public class CertificationController {

    private final CertificationService certificationService;
    private final CertificationMapper certificationMapper;

    @Autowired
    public CertificationController(CertificationService certificationService, CertificationMapper certificationMapper) {
        this.certificationService = certificationService;
        this.certificationMapper = certificationMapper;
    }

    @Operation(summary = "Add certification", responses = {
            @ApiResponse(responseCode = "201", description = "Certification added successfully")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR')")
    @PostMapping
    public ResponseEntity<CertificationDto> addCertification(@Valid @RequestBody CertificationDto request) {
        var cert = certificationService.addCertification(request);
        return ResponseEntity.status(201).body(certificationMapper.toDto(cert));
    }

    @Operation(summary = "Get certifications for employee", responses = {
            @ApiResponse(responseCode = "200", description = "List of certifications")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'HR', 'SUPERVISOR', 'WORKER')")
    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<CertificationDto>> getCertificationsByEmployee(@PathVariable Long employeeId) {
        var certs = certificationService.getCertificationsByEmployee(employeeId);
        return ResponseEntity.ok(certificationMapper.toDtoList(certs));
    }
}

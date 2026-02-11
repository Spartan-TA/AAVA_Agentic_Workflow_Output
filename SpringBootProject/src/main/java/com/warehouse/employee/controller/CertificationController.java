package com.warehouse.employee.controller;

import com.warehouse.employee.dto.CertificationDto;
import com.warehouse.employee.service.CertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * REST controller for certifications.
 */
@RestController
@RequestMapping("/api/certifications")
@Validated
public class CertificationController {

    private final CertificationService certificationService;

    @Autowired
    public CertificationController(CertificationService certificationService) {
        this.certificationService = certificationService;
    }

    @Operation(summary = "Add a certification for an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Certification added successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input")
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @PostMapping
    public ResponseEntity<CertificationDto> addCertification(@Valid @RequestBody CertificationDto dto) {
        CertificationDto response = certificationService.addCertification(dto);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get certifications expiring soon for an employee")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "List of expiring certifications")
    })
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER','EMPLOYEE')")
    @GetMapping("/expiring/{employeeId}")
    public ResponseEntity<List<CertificationDto>> getExpiringSoon(@PathVariable Long employeeId,
                                                                 @RequestParam(defaultValue = "30") int days) {
        List<CertificationDto> expiring = certificationService.checkExpiringSoon(employeeId, days);
        return ResponseEntity.ok(expiring);
    }
}

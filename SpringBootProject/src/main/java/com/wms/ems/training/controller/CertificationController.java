package com.wms.ems.training.controller;

import com.wms.ems.training.dto.CertificationDto;
import com.wms.ems.training.service.CertificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/certifications")
@RequiredArgsConstructor
@Tag(name = "Certifications", description = "Endpoints for employee certifications management")
public class CertificationController {
    private final CertificationService certificationService;

    @Operation(summary = "Create certification")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> createCertification(@Valid @RequestBody CertificationDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(certificationService.createCertification(dto));
    }

    @Operation(summary = "Get all certifications")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<CertificationDto>> getCertifications() {
        return ResponseEntity.ok(certificationService.getCertifications());
    }

    @Operation(summary = "Update certification")
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> updateCertification(@PathVariable Long id, @Valid @RequestBody CertificationDto dto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return ResponseEntity.badRequest().body(bindingResult.getAllErrors());
        }
        return ResponseEntity.ok(certificationService.updateCertification(id, dto));
    }

    @Operation(summary = "Delete certification")
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> deleteCertification(@PathVariable Long id) {
        certificationService.deleteCertification(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get expiring certifications")
    @GetMapping("/expiring")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<List<CertificationDto>> getExpiringCertifications(@RequestParam(required = false) Integer days) {
        return ResponseEntity.ok(certificationService.getExpiringCertifications(days));
    }
}

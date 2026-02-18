package com.companyname.wem.training.controller;

import com.companyname.wem.training.domain.Certification;
import com.companyname.wem.training.dto.CertificationDTO;
import com.companyname.wem.training.service.CertificationService;
import jakarta.validation.Valid;
lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/certifications")
@RequiredArgsConstructor
public class CertificationController {
    private final CertificationService service;

    @PostMapping
    public ResponseEntity<Certification> create(@Valid @RequestBody CertificationDTO dto) {
        Certification cert = service.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(cert);
    }

    @GetMapping("/expiring")
    public ResponseEntity<List<Certification>> getExpiring(@RequestParam(defaultValue = "30") int days) {
        return ResponseEntity.ok(service.getExpiringCertifications(days));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<Certification>> getEmployeeCertifications(@PathVariable Long employeeId) {
        return ResponseEntity.ok(service.getEmployeeCertifications(employeeId));
    }
}

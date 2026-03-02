package com.wems.certification.controller;

import com.wems.certification.domain.Certification;
import com.wems.certification.domain.EmployeeCertification;
import com.wems.certification.service.CertificationService;
import com.wems.employee.domain.Employee;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/certifications")
public class CertificationController {
    @Autowired
    private CertificationService certificationService;

    @PostMapping
    public Certification addCertification(@RequestParam String name, @RequestParam String description) {
        return certificationService.addCertification(name, description);
    }

    @GetMapping("/employee/{id}/certifications")
    public boolean isEmployeeCertified(@PathVariable Long id, @RequestParam Long certificationId) {
        Employee employee = null; // TODO: resolve employee
        return certificationService.isEmployeeCertified(employee, certificationId);
    }

    @PostMapping("/{id}/renew")
    public EmployeeCertification renewCertification(@PathVariable Long id, @RequestParam String newExpiryDate) {
        return certificationService.renewCertification(id, LocalDate.parse(newExpiryDate));
    }
}

package com.companyname.wem.training.service;

import com.companyname.wem.employee.domain.Employee;
import com.companyname.wem.employee.repository.EmployeeRepository;
import com.companyname.wem.training.domain.Certification;
import com.companyname.wem.training.dto.CertificationDTO;
import com.companyname.wem.training.repository.CertificationRepository;
lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificationService {
    private final CertificationRepository repository;
    private final EmployeeRepository employeeRepository;

    @Transactional
    public Certification create(CertificationDTO dto) {
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
            .orElseThrow(() -> new RuntimeException("Employee not found"));
        
        Certification cert = Certification.builder()
            .name(dto.getName())
            .issueDate(dto.getIssueDate())
            .expiryDate(dto.getExpiryDate())
            .employee(employee)
            .documentUrl(dto.getDocumentUrl())
            .build();
        
        return repository.save(cert);
    }

    public List<Certification> getExpiringCertifications(int daysAhead) {
        LocalDate futureDate = LocalDate.now().plusDays(daysAhead);
        return repository.findByExpiryDateBefore(futureDate);
    }

    public List<Certification> getEmployeeCertifications(Long employeeId) {
        return repository.findByEmployeeId(employeeId);
    }
}

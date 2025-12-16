package com.companyname.wems.certification.repository;

import com.companyname.wems.certification.model.EmployeeCertification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<EmployeeCertification, Long> {
    List<EmployeeCertification> findByEmployeeId(Long employeeId);
    List<EmployeeCertification> findByExpiryDateBetween(LocalDate start, LocalDate end);
    List<EmployeeCertification> findByStatus(String status);
}
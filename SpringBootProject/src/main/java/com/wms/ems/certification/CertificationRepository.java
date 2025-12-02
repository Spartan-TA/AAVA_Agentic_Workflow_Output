package com.wms.ems.certification;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByEmployeeId(Long employeeId);
    List<Certification> findByExpiryDateBetween(LocalDate start, LocalDate end);
    List<Certification> findByExpiryDateBefore(LocalDate date);
}

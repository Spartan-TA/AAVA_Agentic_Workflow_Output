package com.warehouse.training.repository;

import com.warehouse.training.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    List<Certification> findByEmployeeId(Long employeeId);
    List<Certification> findByExpiryDateBefore(LocalDate date);
    List<Certification> findByStatus(Certification.Status status);
}

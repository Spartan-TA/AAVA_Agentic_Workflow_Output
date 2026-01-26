package com.example.warehouse.certification.repository;

import com.example.warehouse.certification.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    // Find certifications by employee
    List<Certification> findByEmployeeId(Long employeeId);
}

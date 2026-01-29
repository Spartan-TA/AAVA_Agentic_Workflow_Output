package com.warehouse.employee.management.certification.repository;

import com.warehouse.employee.management.certification.domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificationRepo extends JpaRepository<Certification, Long> {
    // Custom query methods if needed
}

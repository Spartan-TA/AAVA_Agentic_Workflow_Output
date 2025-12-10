package com.warehouse.ems.repository;

import com.warehouse.ems.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    // Custom query methods can be added here
}

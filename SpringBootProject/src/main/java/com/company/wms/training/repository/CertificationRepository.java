package com.company.wms.training.repository;

import com.company.wms.training.model.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Repository interface for Certification entity.
 */
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    // Additional query methods if needed
}

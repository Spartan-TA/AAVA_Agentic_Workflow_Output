package com.wms.certification.repositories;

import com.wms.certification.model.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository for managing Certification entities
 */
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    /**
     * Find certification by name
     * @param name Certification name
     * @return Optional Certification
     */
    Optional<Certification> findByName(String name);
}

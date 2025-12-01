package com.wms.ems.training.repository;

import com.wms.ems.training.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.time.LocalDate;

/**
 * Repository interface for Certification entity.
 * Provides CRUD operations and custom queries for certifications.
 */
@Repository
public interface CertificationRepository extends JpaRepository<Certification, Long> {
    /**
     * Find all certifications for a specific employee.
     * @param employeeId the employee's ID
     * @return List of Certification
     */
    List<Certification> findByEmployeeId(Long employeeId);

    /**
     * Find all certifications expiring before a given date.
     * @param expiryDate the expiry date
     * @return List of Certification
     */
    List<Certification> findByExpiryDateBefore(LocalDate expiryDate);
}

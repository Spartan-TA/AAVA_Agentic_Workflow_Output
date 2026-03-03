package com.wms.ems.training.repository;

import com.wms.ems.training.entity.Certification;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

/**
 * Repository interface for Certification entity operations.
 * Provides CRUD operations and custom queries for certification management.
 */
public interface CertificationRepository extends JpaRepository<Certification, Long> {

    /**
     * Finds certifications by employee ID.
     * @param employeeId the employee ID
     * @return a list of certifications
     */
    List<Certification> findByEmployeeId(Long employeeId);

    /**
     * Finds certifications expiring before a given date.
     * @param date the date
     * @return a list of certifications
     */
    List<Certification> findByExpiryDateBefore(LocalDate date);

    /**
     * Finds certifications expiring between two dates.
     * @param start the start date
     * @param end the end date
     * @return a list of certifications
     */
    List<Certification> findByExpiryDateBetween(LocalDate start, LocalDate end);
}
